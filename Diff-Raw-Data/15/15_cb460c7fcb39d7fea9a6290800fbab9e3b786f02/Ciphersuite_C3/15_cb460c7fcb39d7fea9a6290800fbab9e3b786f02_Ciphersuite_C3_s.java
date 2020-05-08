 package se.kth.ssvl.tslab.wsn.general.servlib.security;
 
 import static se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockProcessor.BP_FAIL;
 import static se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockProcessor.BP_SUCCESS;
 
 import java.io.File;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bouncycastle.crypto.engines.AESEngine;
 import org.bouncycastle.crypto.modes.GCMBlockCipher;
 import org.bouncycastle.crypto.params.AEADParameters;
 import org.bouncycastle.crypto.params.KeyParameter;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.SDNV;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfo;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfoVec;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockProcessor.mutate_func;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockProcessor.mutate_func_event_data;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleDaemon;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol.status_report_reason_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.common.ServlibEventData;
 import se.kth.ssvl.tslab.wsn.general.servlib.contacts.links.Link;
 import se.kth.ssvl.tslab.wsn.general.servlib.naming.endpoint.EndpointID;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.BufferHelper;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.IByteBuffer;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.SerializableByteBuffer;
 
 /**
  * Implementation of the ciphersuite PCB-RSA-AES128-PAYLOAD-PIB-PCB, as defined
  * in the Bundle Security Protocol Specification. It contains the code that does
  * the "real" encryption work. For outgoing encrypted bundles, it implements the
  * required methods: generate, prepare and finalize. For incoming bundles, it
  * implements the validate method.
  */
 public class Ciphersuite_C3 extends Ciphersuite {
 
 	public static final int op_invalid = 0;
 	public static final int op_encrypt = 1;
 	public static final int op_decrypt = 2;
 
 	private byte[] decMsg = null;
 	private byte[] encMsg = null;
 	int decMsg_length;
 	int in_encr_length;
 	private int decLen;
 	private int encLen;
 	static SecureRandom random = new SecureRandom();
 
 	private IByteBuffer tag = new SerializableByteBuffer(16); // 128 bits
 																// recommended
 
 	// Constructor
 	public Ciphersuite_C3() {
 
 	}
 
 	private static String TAG = "Ciphersuite_C3";
 
 	public int cs_num() {
 		return CSNUM_C3;
 
 	}
 
 	/**
 	 * "First callback for parsing blocks that is expected to append a chunk of
 	 * the given data to the given block. When the block is completely received,
 	 * this should also parse the block into any fields in the bundle class.
 	 * 
 	 * The base class implementation parses the block preamble fields to find
 	 * the length of the block and copies the preamble and the data in the
 	 * block's contents buffer.
 	 * 
 	 * This and all derived implementations must be able to handle a block that
 	 * is received in chunks, including cases where the preamble is split into
 	 * multiple chunks."[DTN2]
 	 * 
 	 * @param bundle
 	 *            (OUT): Bundle to set data after consuming
 	 * @param block
 	 *            (OUT): security block to set data after consuming
 	 * @param buf
 	 *            (IN): Populated buffer to read data from
 	 * @param len
 	 *            (IN): Number of bytes to consume
 	 * @return "the amount of data consumed or -1 on error"
 	 * 
 	 */
 	public int consume(Bundle bundle, BlockInfo block, IByteBuffer buf, int len) {
 		BPF.getInstance().getBPFLogger().debug(TAG, "consume()");
 		int cc = block.owner().consume(bundle, block, buf, len);
 
 		if (cc == -1) {
 			return -1; // protocol error
 		}
 
 		// in on-the-fly scenario, process this data for those interested
 
 		if (!block.complete()) {
			assert (cc == (int) len);
 
 			return cc;
 		}
 
 		if (block.locals() == null) // then we need to parse it
 		{
 			parse(block);
 		}
 
 		return cc;
 	}
 
 	/**
 	 * Decode the value from the buffer and return it. IMPORTANT: It updates the
 	 * buffer position after reading.
 	 * 
 	 * @param buf
 	 *            Buffer to read encoded value form
 	 * @param val
 	 *            Get the empty array and set the decoded value on the first
 	 *            index of array
 	 * @return Number of bytes decoded
 	 */
 
 	public static int read_sdnv(IByteBuffer buf, long[] val) {
 		int sdnv_len = SDNV.decode(buf, val);
 		if (sdnv_len < 0) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Block processor too short");
 		}
 		assert (sdnv_len < 0) : TAG + "read sdnv: incorrect length";
 
 		return sdnv_len;
 	}
 
 	/**
 	 * "Validate the block. This is called after all blocks in the bundle have
 	 * been fully received."[DTN2]
 	 * 
 	 * @param bundle
 	 * @param block_list
 	 * @param block
 	 *            (IN): the block with the BP_Local_CS data already loaded.
 	 * @param reception_reason
 	 * @param deletion_reason
 	 * @return : true if the block passes validation
 	 */
 	public boolean validate(final Bundle bundle, BlockInfoVec block_list,
 			BlockInfo block, status_report_reason_t[] reception_reason,
 			status_report_reason_t[] deletion_reason) {
 		/*
 		 * 1. do we have security-dest? If yes, get it, otherwise get
 		 * bundle-dest 2. does it match local_eid ?? 3. if not, return true 4.
 		 * if it does match, parse and validate the block 5. the actions must
 		 * exactly reverse the transforming changes made in finalize()
 		 */
		Bundle deliberate_const_cast_bundle = (Bundle) (bundle);
 		BP_Local_CS locals = (BP_Local_CS) (block.locals());
 		int offset;
 		int len;
 		IByteBuffer key = new SerializableByteBuffer(key_len); // use AES128
 																// 16-byte key
 		IByteBuffer salt = new SerializableByteBuffer(salt_len); // salt for GCM
 		IByteBuffer iv = new SerializableByteBuffer(iv_len); // GCM "iv" length
 																// is 8 bytes
 		IByteBuffer nonce = new SerializableByteBuffer(nonce_len); // 12 bytes
 																	// recommended
 		IByteBuffer buf;
 		IByteBuffer ptr;
 		int sdnv_len = 0; // use an int to handle -1 return values
 		ciphersuite_fields_t item_type;
 		long field_length = 0;
 		long frag_offset_; // Offset of fragment in the original bundle
 		long orig_length_; // Length of original bundle
 		IByteBuffer db;
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "validate()");
 
 		if (locals == null) // FAIL_IF_NULL
 		{
 			return false;
 		}
 
 		if (Ciphersuite.destination_is_local_node(bundle, block)) { // yes,
 																	// this is
 																	// ours so
 																	// go to
 																	// work
 
 			/*
 			 * we expect this to be the "first" block, and there might or might
 			 * not be others. But we should get to this one first and, during
 			 * the processing, convert any other C3 blocks to their
 			 * unencapsulated form. That is, when this call is over, there
 			 * should be no more blocks for us to deal with. Any remaining C3
 			 * block should be for a nested instance
 			 */
 
 			// get pieces from params -- salt, iv, range,
 			buf = new SerializableByteBuffer(locals.security_params()
 					.capacity());
 			BufferHelper.copy_data(buf, buf.position(), locals
 					.security_params(), 0, locals.security_params().capacity());
 
 			len = locals.security_params().capacity();
 
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"validate() locals.correlator() "
 									+ locals.correlator());
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "validate() security params, len = " + len);
 
 			while (len > 0) {
 				// we parse the params: type-length-value
 
 				// we obtain the ciphersuite_fields_t (IV, key info, etc) inside
 				// the params field.
 				item_type = ciphersuite_fields_t.get(buf.get());
 				--len;
 
 				long[] value = new long[1];
 				sdnv_len = read_sdnv(buf, value);
 				field_length = value[0];
 				len -= sdnv_len;
 
 				switch (item_type) {
 				case CS_IV_field: {
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"validate() iv item, len = " + field_length);
 					BufferHelper.copy_data(iv, 0, buf, buf.position(), iv_len);
 
 					buf.position((int) (buf.position() + field_length));
 					len -= field_length;
 				}
 					break;
 
 				case CS_C_block_salt: {
 
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"validate() salt item, len = "
 											+ field_length);
 
 					BufferHelper.copy_data(salt, 0, buf, buf.position(),
 							nonce_len - iv_len);
 					buf.position((int) (buf.position() + field_length));
 					len -= field_length;
 				}
 					break;
 
 				case CS_fragment_offset_and_length_field: {
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"validate() frag info item, len = "
 											+ field_length);
 
 					value = new long[1];
 					sdnv_len = read_sdnv(buf, value);
 					frag_offset_ = value[0];
 
 					len -= sdnv_len;
 
 					value = new long[1];
 					sdnv_len = read_sdnv(buf, value);
 					orig_length_ = value[0];
 
 					len -= sdnv_len;
 				}
 					break;
 
 				default: // deal with improper items
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"validate: unexpected item type "
 											+ item_type + " in security_params");
 					if (locals != null) // GOTO FAIL
 						locals.set_proc_flags((short) (proc_flags_t.CS_BLOCK_FAILED_VALIDATION
 								.getCode() | proc_flags_t.CS_BLOCK_COMPLETED_DO_NOT_FORWARD
 								.getCode()));
 					return false;
 				}
 			}
 
 			// We parse the security-result field.
 			// get pieces from results -- key, icv
 
 			buf = new SerializableByteBuffer(locals.security_result()
 					.capacity());
 			BufferHelper.copy_data(buf, buf.position(), locals
 					.security_result(), 0, locals.security_result().capacity());
 
 			len = locals.security_result().capacity();
 
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "validate(). security result, len = " + len);
 			while (len > 0) {
 				BPF.getInstance().getBPFLogger()
 						.debug(TAG, "remaining len = " + len);
 				// we get the type of the tuple (type-length-value)
 				item_type = ciphersuite_fields_t.get(buf.get());
 				--len;
 
 				// we get the length
 				long[] value = new long[1];
 				sdnv_len = read_sdnv(buf, value);
 
 				field_length = value[0];
 				BPF.getInstance().getBPFLogger()
 						.debug(TAG, "field length: " + field_length);
 				len -= sdnv_len;
 
 				// we process the value (type-length-value)
 				switch (item_type) {
 				case CS_key_ID_field: {
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "validate() key ID item");
 					BPF.getInstance().getBPFLogger()
 							.error(TAG, "not in specs!!!");
 				}
 					break;
 
 				case CS_encoded_key_field: {
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "validate(). Parsing encoded key item");
 					db = new SerializableByteBuffer(16);
 
 					try {
 						KeySteward.decrypt(buf, field_length, db);
 					} catch (Exception e) {
 						BPF.getInstance()
 								.getBPFLogger()
 								.error(TAG,
 										"The key could not be decrypted. Exception: "
 												+ e.getMessage());
 						e.printStackTrace();
 						return false;
 					}
 					// decrypt DOES NOT MOVE the buf position!
 					BufferHelper.copy_data(key, 0, db, 0, key_len);
 					buf.position((int) (buf.position() + field_length));
 					len -= field_length;
 				}
 					break;
 
 				case CS_C_block_ICV_field: {
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "validate(). Parsing icv item");
 
 					BufferHelper.copy_data(tag, tag.position(), buf,
 							buf.position(), tag_len);
 					buf.position((int) (buf.position() + field_length));
 					len -= field_length;
 				}
 					break;
 
 				case CS_encap_block_field: {
 					// don't think we should have one of these here,
 					// only in the correlated blocks
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"validate(). unexpected encap block in security_result");
 					if (locals != null) // GOTO FAIL
 						locals.set_proc_flags((short) (proc_flags_t.CS_BLOCK_FAILED_VALIDATION
 								.getCode() | proc_flags_t.CS_BLOCK_COMPLETED_DO_NOT_FORWARD
 								.getCode()));
 					return false;
 				}
 
 				default: // deal with improper items
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"validate: unexpected item type item_type in security_result");
 					if (locals != null) // GOTO FAIL
 						locals.set_proc_flags((short) (proc_flags_t.CS_BLOCK_FAILED_VALIDATION
 								.getCode() | proc_flags_t.CS_BLOCK_COMPLETED_DO_NOT_FORWARD
 								.getCode()));
 					return false;
 				}
 			}
 
 			// prepare context - one time for all usage here
 			// gcm_init_and_key(key, key_len, (ctx_ex.c));
 			// ctx_ex.operation = op_decrypt;
 
 			// we have the necessary pieces from params and result so now
 			// walk all the blocks and do the various processing things needed.
 			// First is to get the iterator to where we are (see note in
 			// "generate()"
 			// for why we do this)
 
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "validate(). walk block list");
 
 			boolean process_blocks = false; // flag to skip the correlated
 											// blocks in the for loop
 
 			Iterator<BlockInfo> blocks_iter = block_list.iterator();
 			assert (blocks_iter.hasNext());
 			BlockInfo iter;
 			while (blocks_iter.hasNext()) {
 				iter = blocks_iter.next();
 
 				// step over all blocks up to and including the one which
 				// prompted this call, pointed at by "block" argument
 				if (!process_blocks) {
 					if (iter == block)
 						process_blocks = true;
 					continue;
 				}
 
 				switch (iter.type()) {
 
 				case CONFIDENTIALITY_BLOCK: // for Nested Confidentiality blocks
 				{
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "validate() C block");
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"Nested Confidentiality blocks. Skipping...");
 				}
 
 					break;
 
 				case PAYLOAD_BLOCK: {
 					System.gc();
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "validate(). PAYLOAD_BLOCK");
 
 					// nonce is 12 bytes, first 4 are salt (same for all blocks)
 					// and last 8 bytes are per-block IV. The final 4 bytes in
 					// the full block-sized field are, of course, the counter
 					// which is not represented here
 
 					ptr = nonce;
 
 					BufferHelper.copy_data(ptr, ptr.position(), salt, 0,
 							salt_len);
 					ptr.position(ptr.position() + salt_len);
 
 					BufferHelper.copy_data(ptr, ptr.position(), iv, 0, iv_len);
 
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									String.format(
 											"validate(). Nonce: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h ",
 											unsignedByteToInt(nonce.get(0)),
 											unsignedByteToInt(nonce.get(1)),
 											unsignedByteToInt(nonce.get(2)),
 											unsignedByteToInt(nonce.get(3)),
 											unsignedByteToInt(nonce.get(4)),
 											unsignedByteToInt(nonce.get(5)),
 											unsignedByteToInt(nonce.get(6)),
 											unsignedByteToInt(nonce.get(7)),
 											unsignedByteToInt(nonce.get(8)),
 											unsignedByteToInt(nonce.get(9)),
 											unsignedByteToInt(nonce.get(10)),
 											unsignedByteToInt(nonce.get(11))));
 
 					offset = iter.data_offset();
 					len = iter.data_length();
 
 					byte[] temp_key_array = new byte[key_len];
 					key.get(temp_key_array);
 					key.rewind();
 
 					byte[] temp_nonce_array = new byte[iv_len + salt_len];
 					nonce.rewind();
 					nonce.get(temp_nonce_array);
 					nonce.rewind();
 
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									String.format(
 											"validate() symmetric key: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h",
 											unsignedByteToInt(temp_key_array[0]),
 											unsignedByteToInt(temp_key_array[1]),
 											unsignedByteToInt(temp_key_array[2]),
 											unsignedByteToInt(temp_key_array[3]),
 											unsignedByteToInt(temp_key_array[4]),
 											unsignedByteToInt(temp_key_array[5]),
 											unsignedByteToInt(temp_key_array[6]),
 											unsignedByteToInt(temp_key_array[7]),
 											unsignedByteToInt(temp_key_array[8]),
 											unsignedByteToInt(temp_key_array[9]),
 											unsignedByteToInt(temp_key_array[10]),
 											unsignedByteToInt(temp_key_array[11]),
 											unsignedByteToInt(temp_key_array[12]),
 											unsignedByteToInt(temp_key_array[13]),
 											unsignedByteToInt(temp_key_array[14]),
 											unsignedByteToInt(temp_key_array[15])));
 
 					AEADParameters parameters = new AEADParameters(
 							new KeyParameter(temp_key_array), 128,
 							temp_nonce_array, null);
 					GCMBlockCipher gcmEngine = new GCMBlockCipher(
 							new AESEngine());
 					gcmEngine.init(false, parameters);
 
 					offset = iter.data_offset();
 					len = iter.data_length();
 
 					// crypto function
 					mutate_func do_encrypt = new mutate_func() {
 
 						/**
 						 * do_crypt decryption
 						 * 
 						 * @param bundle
 						 * @param caller_block
 						 * @param target_block
 						 * @param buf
 						 *            (IN/OUT): contains the payload, and after
 						 *            encryption, contains the encrypted data.
 						 * @param len
 						 *            (IN): length to decrypt
 						 */
 						@Override
 						public boolean action(ServlibEventData data) {
 							// initialization
 							mutate_func_event_data do_crypt_data = (mutate_func_event_data) data;
 
 							int len = bundle.payload().length();
 							GCMBlockCipher gcmEngine = do_crypt_data.context();
 							System.gc();
 							decMsg = new byte[gcmEngine.getOutputSize(len
 									+ tag_len)];
 
 							byte[] encMsg = new byte[bundle.payload().length()
 									+ tag_len];
 							bundle.payload().read_data(0,
 									bundle.payload().length(), encMsg);
 							int in_array_off = 0;
 
 							for (int i = len; i < len + tag_len; i++)
 								encMsg[i] = tag.get(i - len);
 
 							assert (in_array_off == 0);
 
 							String encr_payl = "";
 							for (int i = 0; i < len && i < 10; i++)
 								encr_payl = new String(encr_payl
 										+ String.format("%2.2h ",
 												unsignedByteToInt(encMsg[i])));
 
 							BPF.getInstance()
 									.getBPFLogger()
 									.debug(TAG,
 											"do_crypt(): Encrypted payload (first 10 bytes max): 0x "
 													+ encr_payl);
 
 							decLen = gcmEngine.processBytes(encMsg, 0,
 									encMsg.length, decMsg, 0);
 
 							BPF.getInstance()
 									.getBPFLogger()
 									.debug(TAG,
 											"do_crypt(): operation decryption, len "
 													+ len);
 
 							return (len > 0) ? true : false;
 						}
 					};
 
 					iter.owner().mutate(do_encrypt,
 							deliberate_const_cast_bundle, block, iter, offset,
 							len, gcmEngine);
 
 					try {
 						decLen += gcmEngine.doFinal(decMsg, decLen);
 
 						BPF.getInstance()
 								.getBPFLogger()
 								.info(TAG,
 										String.format(
 												"validate(): Tag comparison successful for tag: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h ",
 												unsignedByteToInt(tag.get(0)),
 												unsignedByteToInt(tag.get(1)),
 												unsignedByteToInt(tag.get(2)),
 												unsignedByteToInt(tag.get(3)),
 												unsignedByteToInt(tag.get(4)),
 												unsignedByteToInt(tag.get(5)),
 												unsignedByteToInt(tag.get(6)),
 												unsignedByteToInt(tag.get(7)),
 												unsignedByteToInt(tag.get(8)),
 												unsignedByteToInt(tag.get(9)),
 												unsignedByteToInt(tag.get(10)),
 												unsignedByteToInt(tag.get(11)),
 												unsignedByteToInt(tag.get(12)),
 												unsignedByteToInt(tag.get(13)),
 												unsignedByteToInt(tag.get(14)),
 												unsignedByteToInt(tag.get(15))));
 					} catch (Exception e) {
 						BPF.getInstance().getBPFLogger()
 								.error(TAG, "validate: tag comparison failed");
 						if (locals != null) // GOTO FAIL
 							locals.set_proc_flags((short) (proc_flags_t.CS_BLOCK_FAILED_VALIDATION
 									.getCode() | proc_flags_t.CS_BLOCK_COMPLETED_DO_NOT_FORWARD
 									.getCode()));
 						return false;
 					}
 
 					// update payload, swapping the new with the old.
 					IByteBuffer temp_buf = new SerializableByteBuffer(
 							decMsg.length);
 					String payload_hex = "";
 					String payload_str = "";
 					for (int i = 0; i < decMsg.length; i++) {
 						temp_buf.put(decMsg[i]);
 
 					}
 
 					for (int i = 0; i < decMsg.length && i < 10; i++) {
 
 						payload_hex = new String(payload_hex
 								+ String.format("%2.2h ",
 										unsignedByteToInt(decMsg[i])));
 						payload_str = new String(payload_str
 								+ String.format("%c ",
 										unsignedByteToInt(decMsg[i])));
 					}
 
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"validate(): Decrypted Plaintext (first 10 max)[hex]: 0x "
 											+ payload_hex);
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"validate(): Decrypted Plaintext message (first 10 max)[string]: "
 											+ payload_str);
 
 					temp_buf.rewind();
 
 					bundle.payload().write_data(temp_buf, 0, decMsg.length); // (from
 																				// payloadblockprocessor)
 
 					temp_buf = null;
 					decMsg = null;
 					buf = null;
 					encMsg = null;
 					System.gc();
 				}
 					break;
 
 				default:
 					continue;
 
 				} // end switch
 			} // end for
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "validate() walk block list done");
 			locals.set_proc_flag((proc_flags_t.CS_BLOCK_PASSED_VALIDATION
 					.getCode() | proc_flags_t.CS_BLOCK_COMPLETED_DO_NOT_FORWARD
 					.getCode()));
 		} else {
 			// not for here so we didn't check this block
 			locals.set_proc_flag(proc_flags_t.CS_BLOCK_DID_NOT_FAIL.getCode());
 		}
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "validate()  done");
 		return true;
 	}
 
 	private void prepare_has_failed(BP_Local_CS locals) {
 		if (locals != null)
 			locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 					.getCode());
 	}
 
 	/**
 	 * First callback to generate blocks for the output pass. It creates an
 	 * empty blockInfo and adds it to the correct place in the BlockInfoVec
 	 * list.
 	 * 
 	 */
 	public int prepare(final Bundle bundle, BlockInfoVec xmit_blocks,
 			final BlockInfo source, final Link link, BlockInfo.list_owner_t list) {
 		BPF.getInstance().getBPFLogger().debug(TAG, "prepare()");
 		int result = BP_FAIL;
 		short cs_flags = 0;
 		BP_Local_CS locals = null;
 		BP_Local_CS source_locals = null;
 		EndpointID local_eid = BundleDaemon.getInstance().local_eid();
 
 		if ((source != null)
 				&& (((BP_Local_CS) source.locals()).security_dest()
 						.equals(local_eid.toString()))) {
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "prepare() - not being forwarded");
 			return BP_SUCCESS; // it was for us so don't forward
 		}
 
 		// bi is the new security block that we'll add to the list of blocks
 		BlockInfo bi = new BlockInfo(
 				BundleProtocol
 						.find_processor(BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK),
 				source); // null source is OK here
 
 		// If this is a received block then there's not a lot to do yet.
 		// We copy some parameters - the main work is done in generate().
 		// Insertion is at the end of the list, which means that
 		// it will be in the same position as received
 		if (list == BlockInfo.list_owner_t.LIST_RECEIVED) // executed only when
 															// we are bypassing
 															// a security block.
 		{
 			assert (source != null);
 			if (Ciphersuite.destination_is_local_node(bundle, source))
 				return BP_SUCCESS; // don't forward if it's for here
 			xmit_blocks.add(bi);
 			BlockInfo bp = xmit_blocks.back();
 			bp.set_eid_list(source.eid_list());
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"prepare() - forward received block len "
 									+ source.full_length() + " eid_list_count "
 									+ source.eid_list().size() + " new count "
 									+ bp.eid_list().size());
 			if (source.locals() == null)
 				prepare_has_failed(locals); // broken
 			else {
 				source_locals = (BP_Local_CS) (source.locals());
 				if (source_locals == null)
 					prepare_has_failed(locals); // broken
 				else {
 					bp.set_locals(new BP_Local_CS());
 					locals = (BP_Local_CS) (bp.locals());
 					if (locals == null)
 						prepare_has_failed(locals);
 					else {
 						locals.set_owner_cs_num(CSNUM_C3);
 						cs_flags = source_locals.cs_flags();
 						locals.set_list_owner(BlockInfo.list_owner_t.LIST_RECEIVED);
 						locals.set_correlator(source_locals.correlator());
 						IByteBuffer reserved_buffer = BufferHelper
 								.reserve_and_rewind(bp.writable_contents(), 0);
 						bp.set_contents(reserved_buffer);
 
 						// copy security-src and -dest if they exist
 						if ((source_locals.cs_flags() & ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE
 								.getCode()) > 0) {
 							if (source_locals.security_src().length() == 0)
 								prepare_has_failed(locals);
 							else {
 								BPF.getInstance()
 										.getBPFLogger()
 										.debug(TAG,
 												"prepare() add security_src EID");
 								cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE
 										.getCode();
 								locals.set_security_src(source_locals
 										.security_src());
 							}
 
 							if ((source_locals.cs_flags() & ciphersuite_flags_t.CS_BLOCK_HAS_DEST
 									.getCode()) > 0) {
 								if (source_locals.security_dest().length() == 0)
 									prepare_has_failed(locals);
 								else {
 									BPF.getInstance()
 											.getBPFLogger()
 											.debug(TAG,
 													"prepare() add security_dest EID");
 									cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_DEST
 											.getCode();
 									locals.set_security_dest(source_locals
 											.security_dest());
 								}
 								locals.set_cs_flags(cs_flags);
 								BPF.getInstance()
 										.getBPFLogger()
 										.debug(TAG,
 												"prepare() - inserted block eid_list_count "
 														+ bp.eid_list().size());
 								result = BP_SUCCESS;
 								return result;
 							}
 						}
 					}
 				}
 			}
 		} else {
 			// BlockInfo.list_owner_t is not LIST_RECEIVED, i.e. it is a newly
 			// created block.
 
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "prepare() - add new block (or API block etc)");
 			bi.set_locals(new BP_Local_CS());
 			if (bi.locals() == null)
 				prepare_has_failed(locals);
 			else {
 				locals = (BP_Local_CS) (bi.locals());
 				if (locals == null)
 					prepare_has_failed(locals);
 				else {
 					locals.set_owner_cs_num(CSNUM_C3);
 					locals.set_list_owner(list);
 
 					// if there is a security-src and/or -dest, use it -- might
 					// be specified by API
 					if (source != null && source.locals() != null) {
 						locals.set_security_src(((BP_Local_CS) source.locals())
 								.security_src());
 						locals.set_security_dest(((BP_Local_CS) source.locals())
 								.security_dest());
 					}
 
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"prepare() local_eid "
 											+ local_eid.toString()
 											+ " bundle.source_ "
 											+ bundle.source().toString());
 
 					// if not, and we didn't create the bundle, specify
 					// ourselves as sec-src
 					EndpointID tempEID = new EndpointID(bundle.source());
 					tempEID.remove_service_tag();
 					if ((locals.security_src() == null)
 							&& (!local_eid.equals(tempEID)))
 						locals.set_security_src(local_eid.str());
 
 					// if we now have one, add it to list, etc
 					if (locals.security_src() != null) {
 						BPF.getInstance()
 								.getBPFLogger()
 								.debug(TAG,
 										"prepare() add security_src EID "
 												+ locals.security_src()
 														.toString());
 						cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE
 								.getCode();
 						bi.add_eid(new EndpointID(locals.security_src()));
 					}
 
 					if (locals.security_dest() != null) {
 						BPF.getInstance()
 								.getBPFLogger()
 								.debug(TAG,
 										"prepare() add security_dest EID "
 												+ locals.security_dest()
 														.toString());
 						cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_DEST
 								.getCode();
 						bi.add_eid(new EndpointID(locals.security_dest()));
 					}
 
 					locals.set_cs_flags(cs_flags);
 
 					// We should already have the primary block in the list.
 					// We'll insert this after the primary and any BA blocks
 					// and before everything else
 					if (xmit_blocks.size() > 0) {
 
 						Iterator<BlockInfo> iter = xmit_blocks.iterator();
 						int pos = -1;
 						while (iter.hasNext()) {
 							pos++;
 							BlockInfo block_info = iter.next();
 							switch (block_info.type()) {
 							case PRIMARY_BLOCK:
 
 								continue; // go to next for iteration
 
 							default:
 								break; // break of the switch
 							}
 							xmit_blocks.add(pos, bi);
 							break;// break of the while
 						}
 					} else {
 						// it's weird if there are no other blocks but, oh well...
 						xmit_blocks.add(bi);
 					}
 				}
 				result = BP_SUCCESS;
 			}
 		}// end LIST_RECEIVED
 			// if there was an error
 		return result;
 	}
 
 	/**
 	 * Second callback for transmitting a bundle. This pass generates any data
 	 * for the block that does not depend on other blocks' contents. It adds any
 	 * EID references it needs by calling block.add_eid(), then call
 	 * generate_preamble(), which will add the EIDs to the primary block's
 	 * dictionary and write their offsets to this block's preamble.
 	 * 
 	 * More specifically for cryptography, this method populates all the fields
 	 * of the ASB, like salt and so on. After that,it generates the binary data
 	 * ("writable_content") of the ciphersuite-params and security-result, as
 	 * specified in the bundle security protocol. This method doesn't generate
 	 * any encrypted payload, because this is done in the 3rd pass (finalize).
 	 */
 
 	public int generate(final Bundle bundle, BlockInfoVec xmit_blocks,
 			BlockInfo block, final Link link, boolean last) {
 		int result = BP_FAIL;
 		byte[] key = new byte[key_len]; // use AES128 16-byte key
 		byte[] iv = new byte[iv_len]; // AES iv length
 		byte[] salt = new byte[nonce_len - iv_len];
 		short cs_flags = 0;
 		boolean need_correlator = false;
 		long correlator = 0;
 		BP_Local_CS locals = (BP_Local_CS) (block.locals());
 		BP_Local_CS target_locals = null;
 		IByteBuffer ptr;
 		int temp;
 		int rem;
 		IByteBuffer encrypted_key = new SerializableByteBuffer(512);
 		int param_len = 0;
 		int res_len = 0;
 		int length = 0;
 		IByteBuffer buf = new SerializableByteBuffer(256);
 		int len = 0;
 		int sdnv_len = 0; // use an int to handle -1 return values
 		IByteBuffer contents = new SerializableByteBuffer(256);
 		IByteBuffer digest_result = new SerializableByteBuffer(256);
 		IByteBuffer params = new SerializableByteBuffer(256);
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "generate()");
 
 		if (locals == null) {
 			return BP_FAIL;
 		}
 
 		cs_flags = locals.cs_flags(); // get flags from prepare()
 		// if this is a received block then it's easy
 		if (locals.list_owner() == BlockInfo.list_owner_t.LIST_RECEIVED) 
 		 //executed only when we are bypassing a security block.
 		{
 			// generate the preamble and copy the data.
 			length = block.source().data_length();
 
 			generate_preamble(
 					xmit_blocks,
 					block,
 					BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK,
 					BundleProtocol.block_flag_t.BLOCK_FLAG_DISCARD_BUNDLE_ONERROR
 							.getCode()
 							| BundleProtocol.block_flag_t.BLOCK_FLAG_REPLICATE
 									.getCode()
 							| (last ? BundleProtocol.block_flag_t.BLOCK_FLAG_LAST_BLOCK
 									.getCode() : 0), length);
 
 			contents = block.writable_contents();
 			IByteBuffer reserved_buffer = BufferHelper.reserve_and_rewind(
 					contents, block.data_offset() + length);
 
 			block.set_contents(reserved_buffer);
 
 			BufferHelper.copy_data(reserved_buffer, block.data_offset(), block
 					.source().contents(), block.source().data_offset(), length);
 
 			BPF.getInstance().getBPFLogger().debug(TAG, "generate()  done");
 			return BP_SUCCESS;
 		}
 
 		// This block will have a correlator iff there are PSBs or CBs,
 		// no correlator if only a payload and no PSBs or CBs
 
 		Iterator<BlockInfo> iter = xmit_blocks.iterator();
 		assert (iter.hasNext());
 		boolean process_blocks = false; // flag to skip the correlated blocks in
 										// the for loop.
 		for (BlockInfo block_aux = iter.next(); iter.hasNext(); block_aux = iter
 				.next()) {
 			// Advance the iterator to our current position.
 			// Long-winded implementation note:-
 			// we would use "distance" but block isn't
 			// an iterator, just a pointer. Pointer arithmetic
 			// works in some systems but is not always portable
 			// so we don't do that here.
 			if (!process_blocks) {
 				if (block_aux == block)
 					process_blocks = true;
 				continue;
 
 			}
 			
 			if (block_aux.type() == BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK) {
 				target_locals = (BP_Local_CS) (block_aux.locals());
 
 				if (target_locals == null) {
 					locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 							.getCode());
 					return BP_FAIL;
 				}
 				if (target_locals.owner_cs_num() == CSNUM_C3) {
 					need_correlator = true; // yes - we need a correlator
 					break;
 				}
 			}
 		}
 
 		if (need_correlator) {
 			correlator = create_correlator(bundle, xmit_blocks);
 			correlator |= (int) CSNUM_C3 << 16; // add our ciphersuite number
 			locals.set_correlator(correlator);
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "generate() correlator " + correlator);
 		}
 
 		/*
 		 * params field will contain - salt (4 bytes), plus type and length - IV
 		 * (block-length, 8 bytes), plus type and length - fragment offset and
 		 * length, if a fragment-bundle, plus type and length - key-identifier
 		 * (optional, not implemented yet), plus type and length
 		 */
 
 		params = locals.writable_security_params();
 
 		// populate salt and IV
 		random.nextBytes(salt);
 		random.nextBytes(iv);
 
 		// save for finalize()
 		IByteBuffer salt_buff = new SerializableByteBuffer(salt.length);
 		salt_buff.put(salt);
 		salt_buff.rewind();
 		locals.set_salt(salt_buff, salt.length);
 
 		IByteBuffer iv_buff = new SerializableByteBuffer(iv.length);
 		iv_buff.put(iv);
 		iv_buff.rewind();
 		locals.set_iv(iv_buff, iv.length);
 
 		param_len = 1 + 1 + salt.length; // salt: type-lenght-value
 		param_len += 1 + 1 + iv.length; // IV: type-lenght-value
 
 		if (bundle.is_fragment()) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Error. fragments not implemented.");
 		}
 
 		params = BufferHelper.reserve_and_rewind(params, param_len);
 		// will need more if there is a key identifier - TBD
 		locals.set_security_params(params);
 
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "generate() security params, len = %d" + param_len);
 
 		ptr = params;
 
 		ptr.put(ciphersuite_fields_t.CS_C_block_salt.getCode());// type
 
 		SDNV.encode(salt.length, ptr, ptr.capacity());
 
 		salt_buff.rewind();
 		byte[] temp_arr_buf = new byte[salt_buff.capacity()];
 		salt_buff.get(temp_arr_buf);
 		ptr.put(temp_arr_buf); // value
 
 		ptr.put(ciphersuite_fields_t.CS_IV_field.getCode());// type
 		SDNV.encode(iv.length, ptr, ptr.capacity());// Length
 
 		iv_buff.rewind();
 		temp_arr_buf = new byte[iv_buff.capacity()];
 		iv_buff.get(temp_arr_buf);
 		ptr.put(temp_arr_buf); // value
 
 		// Now we calculate the Security-Result
 
 		if (bundle.is_fragment()) {
 			// memcpy(ptr, fragment_item, 2 + temp);
 		}
 
 		// generate actual key
 		random.nextBytes(key);
 
 		// save for finalize()
 		IByteBuffer key_buff = new SerializableByteBuffer(key.length);
 		key_buff.put(key);
 		key_buff.rewind();
 		locals.set_key(key_buff, key.length);
 
 		BPF.getInstance()
 				.getBPFLogger()
 				.debug(TAG,
 						String.format(
 								"generate() random key: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h",
 								unsignedByteToInt(key[0]),
 								unsignedByteToInt(key[1]),
 								unsignedByteToInt(key[2]),
 								unsignedByteToInt(key[3]),
 								unsignedByteToInt(key[4]),
 								unsignedByteToInt(key[5]),
 								unsignedByteToInt(key[6]),
 								unsignedByteToInt(key[7]),
 								unsignedByteToInt(key[8]),
 								unsignedByteToInt(key[9]),
 								unsignedByteToInt(key[10]),
 								unsignedByteToInt(key[11]),
 								unsignedByteToInt(key[12]),
 								unsignedByteToInt(key[13]),
 								unsignedByteToInt(key[14]),
 								unsignedByteToInt(key[15])));
 
 		String sec_dest;
 		if (locals.security_dest() != null)
 			sec_dest = locals.security_dest();
 		else {
 			EndpointID tempEID = new EndpointID(bundle.dest());
 			tempEID.remove_service_tag();
 			sec_dest = tempEID.toString();
 		}
 
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "generate():  sec_dest: " + sec_dest);
 		try { // the key field contains the key to be encrypted
 			KeySteward.encrypt(sec_dest, key, encrypted_key);
 		} catch (Exception e) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.error(TAG,
 							"Exception during key encryption in KeySteward.encrypt(): "
 									+ e.toString());
 		}
 
 		BPF.getInstance()
 				.getBPFLogger()
 				.debug(TAG,
 						"generate(): encrypted_key len: "
 								+ encrypted_key.capacity());
 
 		// we calculate the length of the security-result field: we consider
 		// encrypted key and ICV.
 		res_len = 1 + SDNV.encoding_len(encrypted_key.capacity())
 				+ encrypted_key.capacity();
 		res_len += 1 + 1 + tag_len;
 
 		digest_result = locals.writable_security_result(); // we generate the
 															// security-result
 
 		// digest_result = BufferHelper.reserve_and_rewind (digest_result,
 		// res_len);
 		digest_result = new SerializableByteBuffer(res_len);
 		locals.set_security_result(digest_result);
 		rem = res_len;
 
 		ptr = digest_result;
 		ptr.put(Ciphersuite.ciphersuite_fields_t.CS_encoded_key_field.getCode());
 		rem--;
 		temp = SDNV.encode(encrypted_key.capacity(), ptr, rem);
 
 		rem -= temp;
 		ptr = BufferHelper.reserve(ptr, encrypted_key.capacity());
 		locals.set_security_result(ptr);
 
 		BufferHelper.copy_data(ptr, ptr.position(), encrypted_key, 0,
 				encrypted_key.capacity());
 
 		ptr.position(ptr.position() + encrypted_key.capacity());
 
 		rem -= encrypted_key.capacity();
 
 		// First we need to work out the lengths and create the preamble
 		length = 0;
 		if (need_correlator) {
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "generate() correlator " + correlator);
 			locals.set_correlator(correlator);
 			length += SDNV.encoding_len(locals.correlator());
 			cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_CORRELATOR.getCode();
 		}
 
 		// ciphersuite number and flags
 		cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_PARAMS.getCode();
 		cs_flags |= ciphersuite_flags_t.CS_BLOCK_HAS_RESULT.getCode();
 		locals.set_cs_flags(cs_flags);
 		length += SDNV.encoding_len(CSNUM_C3);
 		length += SDNV.encoding_len(locals.cs_flags());
 
 		param_len = locals.security_params().position();
 		length += SDNV.encoding_len(param_len) + param_len;
 		locals.set_security_result_offset(length); // remember this for
 													// finalize(), when we write
 													// the sec-result in the
 													// binary data of the
 													// security block.
 		length += SDNV.encoding_len(res_len) + res_len;
 
 		contents = block.writable_contents();
 
 		generate_preamble(
 				xmit_blocks,
 				block,
 				BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK,
 				BundleProtocol.block_flag_t.BLOCK_FLAG_DISCARD_BUNDLE_ONERROR
 						.getCode()
 						| (last ? BundleProtocol.block_flag_t.BLOCK_FLAG_LAST_BLOCK
 								.getCode() : 0), length);
 
 		BPF.getInstance()
 				.getBPFLogger()
 				.debug(TAG,
 						"generate() preamble has len: " + block.data_offset()
 								+ ". block has len: " + length);
 
 		contents = BufferHelper.reserve_and_rewind(contents,
 				block.data_offset() + length);
 
 		buf = block.writable_contents();
 		buf.position(block.data_offset());
 		len = length;
 
 		// Assemble data into block contents.
 
 		// ciphersuite number and flags
 
 		sdnv_len = SDNV.encode(locals.owner_cs_num(), buf, len);
 		if (sdnv_len <= 0) {
 			locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 					.getCode());
 			return BP_FAIL;
 		}
 
 		len -= sdnv_len;
 
 		sdnv_len = SDNV.encode(locals.cs_flags(), buf, len);
 		if (sdnv_len <= 0) {
 			locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 					.getCode());
 			return BP_FAIL;
 		}
 
 		len -= sdnv_len;
 
 		if (need_correlator) {
 			// correlator
 			sdnv_len = SDNV.encode(locals.correlator(), buf, len);
 			if (sdnv_len <= 0) {
 				locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 						.getCode());
 				return BP_FAIL;
 			}
 
 			len -= sdnv_len;
 		}
 
 		// length of params
 		sdnv_len = SDNV.encode(param_len, buf, len);
 		if (sdnv_len <= 0) {
 			locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 					.getCode());
 			return BP_FAIL;
 		}
 
 		len -= sdnv_len;
 
 		// params data
 
 		BufferHelper.copy_data(buf, buf.position(), locals.security_params(),
 				0, param_len);
 		buf.position(buf.position() + param_len);
 		len -= param_len;
 
 		// length of result -- we have to put this in now
 		sdnv_len = SDNV.encode(res_len, buf, len);
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "generate() done");
 
 		result = BP_SUCCESS;
 		return result;
 	}
 
 	/**
 	 * Third callback for transmitting a bundle. This pass walks over the
 	 * xmit_blocks and generates security signatures for the security block that
 	 * may depend on other blocks' contents.
 	 * 
 	 * For payload blocks, it encrypts the contents (by previously generating an
 	 * IV), changing the payload. Also, it generates the ICV for this payload
 	 * block, and places/updates the total ICV in the security-result field of
 	 * the first PCB.
 	 */
 	public int finalize(final Bundle bundle, BlockInfoVec xmit_blocks,
 			BlockInfo block, final Link link) {
 		int result = BP_FAIL;
 		Bundle deliberate_const_cast_bundle = bundle;
 		int offset;
 		int len;
 
 		/**
 		 * symetric key. use AES128 16-byte key
 		 */
 		IByteBuffer key = new SerializableByteBuffer(key_len);
 
 		IByteBuffer nonce = new SerializableByteBuffer(nonce_len);// 12 bytes
 																	// recommended
 		/**
 		 * Authentication tag, also called ICV. Result of the encryption, to
 		 * check the integrity of the data.
 		 */
 		IByteBuffer tag = new SerializableByteBuffer(tag_len); // 128 bits
 																// recommended
 
 		IByteBuffer buf = new SerializableByteBuffer(256);
 
 		/**
 		 * aux var to populate the IbyteBuffer fields.
 		 */
 		IByteBuffer ptr = new SerializableByteBuffer(256);
 		BP_Local_CS locals = null;
 		BP_Local_CS target_locals = null;
 
 		ArrayList<Long> correlator_list = new ArrayList<Long>();
 		
 		int sdnv_len = 0; // use an int to handle -1 return values
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "finalize()");
 
 		locals = (BP_Local_CS) (block.locals());
 
 		if (locals == null)
 			return BP_FAIL;
 
 		// if this is a received block then we're done
 		if (locals.list_owner() == BlockInfo.list_owner_t.LIST_RECEIVED)
 			return BP_SUCCESS;
 
 		BufferHelper.copy_data(key, key.position(), locals.key(), 0, key_len);
 
 		// Walk the list and process each of the blocks.
 		// We only change PS, C3 and the payload data,
 		// all others are unmodified
 
 		// Note that we can only process PSBs and C3s that follow this block
 		// as doing otherwise would mean that there would be a
 		// correlator block preceding its parent
 
 		// However this causes a problem if the PS is a two-block scheme,
 		// as we'll convert the second, correlated block to C and then
 		// the PS processor won't have its second block.
 
 		// There can also be tunneling issues, depending upon the
 		// exact sequencing of blocks. It seems best to add C blocks
 		// as early as possible in order to mitigate this problem.
 		// That has its own drawbacks unfortunately
 
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "finalize()  we start walking the list of blocks");
 
 		Iterator<BlockInfo> blocks_iter = xmit_blocks.iterator();
 		assert (blocks_iter.hasNext());
 		boolean process_blocks = false; // flag to skip the correlated blocks in
 										// the for loop.
 
 		while (blocks_iter.hasNext()) {
 			BlockInfo iter = blocks_iter.next();
 
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"finalize()  iteration.next. type of block: "
 									+ iter.type().toString());
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"data offset: " + iter.data_offset()
 									+ " data_length: " + iter.data_length());
 
 			// Advance the iterator to our current position.
 			// While we do it, we also remember the correlator values
 			// of any PSBs or C3 blocks we encounter.
 			// We do this to avoid processing any related correlated blocks
 			// Note that we include the current block in the test below
 			// in order to prevent encapsulating it !!
 
 			if (!process_blocks)
 			{
 				if (iter.type() == BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK) {
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"finalize()  we got a confidentialiity block.skiping");
 					target_locals = (BP_Local_CS) (iter.locals());
 
 					if (target_locals == null)// FAIL_IF_NULL
 					{
 						if (locals != null)
 							locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 									.getCode());
 						return BP_FAIL;
 					}
 
 					if (target_locals.owner_cs_num() == CSNUM_C3) {
 						correlator_list.add(target_locals.correlator());
 					}
 				}
 
 				if (iter == block) {
 					BPF.getInstance()
 							.getBPFLogger()
 							.debug(TAG,
 									"finalize() this block was our payload security block! Next should be payload");
 					process_blocks = true;
 				}
 
 				continue;
 			}
 
 			switch (iter.type()) {
 
 			case CONFIDENTIALITY_BLOCK:
 
 			/*
 			 * For each PIB or PCB to be protected, the entire original block is
 			 * encapsulated in a "replacing" PCB. This replacing PCB is placed
 			 * in the outgoing bundle in the same position as the original
 			 * block, PIB or PCB. As mentioned above, this is one-for-one
 			 * replacement and there is no consolidation of blocks or mixing of
 			 * data in any way.
 			 */
 			{
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								"finalize() PSB or CB after our block. we are going to encapsulate it");
 				BPF.getInstance().getBPFLogger()
 						.debug(TAG, "Encapsulated blocks. Skipping...");
 			}
 				break;
 
 			case PAYLOAD_BLOCK: {
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								"finalize(). Payload block after our block. we are going to encrypt it");
 				/*
 				 * prepare context -- key supplied already nonce is 12 bytes,
 				 * first 4 are salt (same for all blocks) and last 8 bytes are
 				 * per-block IV. The final 4 bytes in the full block-sized field
 				 * are, of course, the counter which is not represented here
 				 * 
 				 * "For the payload, only the bytes of the bundle payload field
 				 * are affected, being replaced by ciphertext. The salt, IV and
 				 * key values specified in the first PCB are used to encrypt the
 				 * payload, and the resultant authentication tag (ICV) is placed
 				 * in an ICV item in the security-result field of that first
 				 * PCB. The other bytes of the payload block, such as type,
 				 * flags and length, are not modified." [dtn-sec draft15]
 				 */
 				int rem;
 				ciphersuite_fields_t type;
 				long field_len;
 
 				ptr = nonce; // Ibytebuffer, empty
 
 				BPF.getInstance().getBPFLogger()
 						.debug(TAG, "finalize() PAYLOAD_BLOCK");
 
 				// we copy the salt from the ASB to the work buffer
 				BufferHelper.copy_data(ptr, ptr.position(), locals.salt(), 0,
 						salt_len);
 
 				ptr.position(ptr.position() + salt_len);
 
 				// we copy the IV from the ASB to the work buffer (generated in
 				// 2nd pass)
 				BufferHelper.copy_data(ptr, ptr.position(), locals.iv(), 0,
 						iv_len);
 
 				ptr.position(ptr.position() + iv_len);
 
 				// We have created the prepare context
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								String.format(
 										"finalize(). nonce: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h ",
 										unsignedByteToInt(nonce.get(0)),
 										unsignedByteToInt(nonce.get(1)),
 										unsignedByteToInt(nonce.get(2)),
 										unsignedByteToInt(nonce.get(3)),
 										unsignedByteToInt(nonce.get(4)),
 										unsignedByteToInt(nonce.get(5)),
 										unsignedByteToInt(nonce.get(6)),
 										unsignedByteToInt(nonce.get(7)),
 										unsignedByteToInt(nonce.get(8)),
 										unsignedByteToInt(nonce.get(9)),
 										unsignedByteToInt(nonce.get(10)),
 										unsignedByteToInt(nonce.get(11))));
 
 				byte[] temp_key_array = new byte[key_len];
 
 				key.get(temp_key_array);
 				key.rewind();
 
 				byte[] temp_nonce_array = new byte[iv_len + salt_len];
 				nonce.rewind();
 				nonce.get(temp_nonce_array);
 				nonce.rewind();
 
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								String.format(
 										"finalize() random key: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h",
 										unsignedByteToInt(temp_key_array[0]),
 										unsignedByteToInt(temp_key_array[1]),
 										unsignedByteToInt(temp_key_array[2]),
 										unsignedByteToInt(temp_key_array[3]),
 										unsignedByteToInt(temp_key_array[4]),
 										unsignedByteToInt(temp_key_array[5]),
 										unsignedByteToInt(temp_key_array[6]),
 										unsignedByteToInt(temp_key_array[7]),
 										unsignedByteToInt(temp_key_array[8]),
 										unsignedByteToInt(temp_key_array[9]),
 										unsignedByteToInt(temp_key_array[10]),
 										unsignedByteToInt(temp_key_array[11]),
 										unsignedByteToInt(temp_key_array[12]),
 										unsignedByteToInt(temp_key_array[13]),
 										unsignedByteToInt(temp_key_array[14]),
 										unsignedByteToInt(temp_key_array[15])));
 
 				AEADParameters parameters = new AEADParameters(
 						new KeyParameter(temp_key_array), 128,
 						temp_nonce_array, null);
 				GCMBlockCipher gcmEngine = new GCMBlockCipher(new AESEngine());
 				gcmEngine.init(true, parameters);
 
 				offset = iter.data_offset();
 				len = iter.data_length();
 
 				// crypto function
 				mutate_func do_crypt = new mutate_func() {
 
 					/**
 					 * do_crypt for encryption
 					 * 
 					 * @param bundle
 					 * @param caller_block
 					 * @param target_block
 					 * @param buf
 					 *            (IN/OUT): contains the payload, and after
 					 *            encryption, contains the encrypted data.
 					 * @param len
 					 *            (IN): length to encrypt
 					 */
 					@Override
 					public boolean action(ServlibEventData data) {
 						// initialization
 						mutate_func_event_data do_crypt_data = (mutate_func_event_data) data;
 
 						int len = do_crypt_data.len();
						GCMBlockCipher gcmEngine = (GCMBlockCipher) do_crypt_data
 								.context();
 						System.gc();
 						encMsg = new byte[gcmEngine.getOutputSize(len)];
 
 						byte[] inMsg = new byte[bundle.payload().length()];
 						bundle.payload().read_data(0,
 								bundle.payload().length(), inMsg);
 						int in_array_off = 0;
 
 						assert (in_array_off == 0);
 						assert (inMsg.length == len);
 
 						String key_str = "";
 						for (int i = 0; (i < inMsg.length && i < 10); i++)
 							key_str = new String(key_str
 									+ String.format("%2.2h ",
 											unsignedByteToInt(inMsg[i])));
 
 						BPF.getInstance()
 								.getBPFLogger()
 								.debug(TAG,
 										"finalize(). Plaintext message (first 10 char max): 0x "
 												+ key_str);
 
 						in_encr_length = inMsg.length;
 						encLen = gcmEngine.processBytes(inMsg, in_array_off,
 								len, encMsg, 0);
 
 						BPF.getInstance()
 								.getBPFLogger()
 								.debug(TAG,
 										"Ciphersuite_C3::do_crypt() operation encryption len "
 												+ len);
 
 						return (len > 0) ? true : false;
 					}
 				};
 
 				iter.owner().mutate(do_crypt,
 						deliberate_const_cast_bundle, block, iter, offset, len,
 						gcmEngine);
 
 				try {
 					encLen += gcmEngine.doFinal(encMsg, encLen);
 				} catch (Exception e) {
 					BPF.getInstance().getBPFLogger().error(TAG, e.toString());
 				}
 
 				System.gc();
 				String encr_payl = "";
 				for (int i = 0; i < in_encr_length && i < 10; i++)
 					encr_payl = new String(encr_payl
 							+ String.format("%2.2h ",
 									unsignedByteToInt(encMsg[i])));
 
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								"generate(): Encrypted payload using symmetric key (10 char max): 0x "
 										+ encr_payl);
 
 				// update payload, swapping the new with the old.
 				IByteBuffer temp_buf = new SerializableByteBuffer(
 						in_encr_length);
 				for (int i = 0; i < in_encr_length; i++)
 					temp_buf.put(encMsg[i]);
 				temp_buf.rewind();
 				bundle.payload().setIsEncrypted(true);
 				bundle.payload()
 						.setFile(
 								new File(
 										bundle.payload().file().getParentFile()
 												.getAbsolutePath()
 												+ "/"
 												+ bundle.payload().file()
 														.getName()
 												+ "_"
 												+ Integer.toString((int) (Math
 														.random() * 10000))));
 				bundle.payload().write_data(temp_buf, 0, in_encr_length);
 
 				// need to update the hash list here ?
 				// BundleStore.getInstance().updateHash(bundle);
 
 				tag.rewind();
 				assert (tag.capacity() == encMsg.length - in_encr_length);
 				for (int i = in_encr_length; i < encMsg.length; i++) {
 					tag.put(encMsg[i]);
 				}
 
 				decMsg_length = encMsg.length;
 				encMsg = null;
 				System.gc();
 
 				tag.rewind();
 
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								String.format(
 										"finalize(): tag: 0x %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h %2.2h ",
 										unsignedByteToInt(tag.get(0)),
 										unsignedByteToInt(tag.get(1)),
 										unsignedByteToInt(tag.get(2)),
 										unsignedByteToInt(tag.get(3)),
 										unsignedByteToInt(tag.get(4)),
 										unsignedByteToInt(tag.get(5)),
 										unsignedByteToInt(tag.get(6)),
 										unsignedByteToInt(tag.get(7)),
 										unsignedByteToInt(tag.get(8)),
 										unsignedByteToInt(tag.get(9)),
 										unsignedByteToInt(tag.get(10)),
 										unsignedByteToInt(tag.get(11)),
 										unsignedByteToInt(tag.get(12)),
 										unsignedByteToInt(tag.get(13)),
 										unsignedByteToInt(tag.get(14)),
 										unsignedByteToInt(tag.get(15))));
 				// get the security-result item, and step over the encrypted key
 				// item
 				IByteBuffer result_buf = new SerializableByteBuffer(256);
 				result_buf = locals.writable_security_result();
 
 				ptr = result_buf;
 
 				rem = result_buf.capacity(); // we save the last position. its
 												// the remaining code.
 				ptr.rewind();
 
 				long[] value = new long[1];
 				SDNV.decode(ptr, ptr.capacity(), value); // type: 3. encoded key
 				type = Ciphersuite.ciphersuite_fields_t.get((byte) value[0]);
 
 				if (!(type
 						.equals(Ciphersuite.ciphersuite_fields_t.CS_encoded_key_field)))// FAIL_IF
 				{
 					if (locals != null)
 						locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 								.getCode());
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"finalize(). We should have found CS_encoded_key_field!!");
 					return BP_FAIL;
 				}
 
 				rem--;
 				value = new long[1];
 				sdnv_len = SDNV.decode(ptr, rem, value);// length= 2. (512)
 				field_len = value[0]; // length of the encrypted key field
 
 				rem -= sdnv_len;
 
 				// We jump the enc_key field.
 				ptr.position((int) (ptr.position() + field_len));
 
 				rem -= field_len;
 
 				if (rem != 1 + 1 + tag_len)// FAIL_IF. remaining code should be
 											// type-length-value of ICV.
 				{
 					if (locals != null)
 						locals.set_proc_flag(proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND
 								.getCode());
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"finalize(). error in if (rem != 1 + 1 + tag_len)");
 					return BP_FAIL;
 				}
 
 				// we are going to create the tuple type-length-value to update
 				// the ICV signature field (which is a security result field) in
 				// the first PCB.
 
 				// we create and insert the type into the work buffer.
 				ptr.put(ciphersuite_fields_t.CS_C_block_ICV_field.getCode());
 				rem--;
 
 				// we insert the tag length into the work buffer
 
 				SDNV.encode(tag_len, ptr);
 
 				rem--;
 				
 				// we insert the tag data into the work buffer
 				BufferHelper.copy_data(ptr, ptr.position(), tag, 0, tag_len);
 				ptr.position(ptr.position() + tag_len);
 
 				// now put the security-result field into the security block
 				// binary contents
 				// which is block.writable_contents();
 
 				IByteBuffer contents = new SerializableByteBuffer(256);
 				;
 				contents = block.writable_contents();
 				buf = contents;
 				rem = contents.position();
 
 				// buf.position(block.data_offset());// we need to add
 				// data_offset as well,
 
 				rem -= block.data_offset(); // since we're pointing at the whole
 											// buffer
 
 				buf.position(block.data_offset()
 						+ locals.security_result_offset()); // and this offset
 															// is just within
 															// the data portion
 															// of the buffer
 				rem -= locals.security_result_offset();
 
 				sdnv_len = SDNV.len(buf); // size of result-length field (sdnv)
 				buf.position(buf.position() + sdnv_len); // "step over that length field".
 															// (we have already
 															// added it in
 															// generate() )
 				rem -= sdnv_len;
 				BufferHelper.copy_data(buf, buf.position(), result_buf, 0,
 						result_buf.position());
 
 				BPF.getInstance().getBPFLogger()
 						.debug(TAG, "finalize(). PAYLOAD_BLOCK done");
 
 			}
 				break; // break from switch, continue for "for" loop
 
 			default:
 				continue;
 			} // end of switch
 		}
 		BPF.getInstance().getBPFLogger().debug(TAG, "finalize() done");
 
 		result = BP_SUCCESS;
 		return result;
 	}
 
 	public static int unsignedByteToInt(byte b) {
 		return (int) b & 0xFF;
 	}
 
 	/**
 	 * Ciphersuite number
 	 * 
 	 */
 	final static short CSNUM_C3 = 3;
 
 	int key_len = 128 / 8; // 16
 	int nonce_len = 12;
 	int salt_len = 4;
 
 	/**
 	 * 
 	 * iv_len is only 8 for GCM, which also uses 4-byte nonce
 	 */
 	int iv_len = nonce_len - salt_len;
 
 	int tag_len = 128 / 8;
 }
