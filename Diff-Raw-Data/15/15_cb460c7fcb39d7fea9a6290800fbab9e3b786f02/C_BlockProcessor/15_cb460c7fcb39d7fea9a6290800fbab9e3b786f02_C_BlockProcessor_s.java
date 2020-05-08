 package se.kth.ssvl.tslab.wsn.general.servlib.security;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfo;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfoVec;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockProcessor;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol.bundle_block_type_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol.status_report_reason_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.contacts.links.Link;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.BufferHelper;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.IByteBuffer;
 
 /**
  * 
  * Block processor implementation for the Payload confidentiality block (PCB).
  * This class extends from BlockProcessor and implements a generic PCB
  * processor. In the current implementation, it calls the methods of
  * Ciphersuite_C3, which is an implementation of the ciphersuite
  * PCB-RSA-AES128-PAYLOAD-PIB-PCB, as defined in the Bundle Security Protocol
  * Specification (RFC6257)
  * 
  */
 public class C_BlockProcessor extends BlockProcessor {
 	/**
 	 * Serial UID to support Java Serializable
 	 */
 	private static final long serialVersionUID = -2101802304411629534L;
 
 	private static String TAG = "C_BlockProcessor";
 
 	// constructor
 	public C_BlockProcessor() {
 		super(bundle_block_type_t.CONFIDENTIALITY_BLOCK);
 	}
 
 	/**
 	 * First callback for parsing blocks that is expected to append a chunk of
 	 * the given data to the given block. When the block is completely received,
 	 * this should also parse the block into any fields in the bundle class.
 	 * 
 	 * This function consumes the C block of the bundle. It is a virtual from
 	 * BlockProcessor.
 	 * 
 	 * @param bundle
 	 *            (OUT): Bundle to set data after consuming
 	 * @param block
 	 *            (OUT): security block to set data after consuming
 	 * @param buf
 	 *            (IN): Populated buffer to read data from
 	 * @param len
 	 *            (IN): Number of bytes to consume
 	 * @return number of bytes successfully consumed, or -1 in case of error
 	 */
 	public int consume(Bundle bundle, BlockInfo block, IByteBuffer buf, int len) {
 		// consume the preamble
 		int cc = super.consume(bundle, block, buf, len);
 
 		if (cc == -1) {
 			return -1; // protocol error
 		}
 
 		// in on-the-fly scenario, process this data for those interested
 
 		if (!block.complete()) {
			assert (cc == (int) len);
 			return cc;
 		}
 
 		Ciphersuite.parse(block);
 
 		return cc;
 	}
 
 	/**
 	 * Perform any needed action in case a block/bundle has been
 	 * reloaded from store
 	 */
 	public boolean reload_post_process(Bundle bundle, BlockInfoVec block_list,
 			BlockInfo block) {
 		// Received blocks might be stored and reloaded and
 		// some fields aren't reset.
 		// This allows BlockProcessors to reestablish what they
 		// need
 
 		Ciphersuite p = null;
 		boolean err = true;
 		bundle_block_type_t type;
 		BP_Local_CS locals;
 
 		if (!block.reloaded())
 			return true;
 
 		type = block.type();
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "C_BlockProcessor.reload block type " + type);
 
 		Ciphersuite.parse(block);
 		locals = (BP_Local_CS) (block.locals());
 
 		if (locals == null)// CS_FAIL_IF_NULL
 			return false;
 
 		p = Ciphersuite.find_suite(locals.owner_cs_num());
 		if (p != null)
 			err = (p.reload_post_process(bundle, block_list, block));
 
 		block.set_reloaded(false);
 		return err;
 	}
 
 	/**
 	 * Validate the block. This is called after all blocks in the bundle have
 	 * been fully received.
 	 * 
 	 * @param bundle
 	 * @param block_list
 	 * @param block
 	 * @param reception_reason
 	 * @param deletion_reason
 	 * @return : true if the block passes validation
 	 * 
 	 */
 	public boolean validate(final Bundle bundle, BlockInfoVec block_list,
 			BlockInfo block, status_report_reason_t[] reception_reason,
 			status_report_reason_t[] deletion_reason) {
 		Ciphersuite_C3 p = null;
 
 		BP_Local_CS locals = (BP_Local_CS) (block.locals());
 		boolean result = false;
 
 		if (locals == null) // CS_FAIL_IF_NULL
 		{
 			deletion_reason[0] = BundleProtocol.status_report_reason_t.REASON_SECURITY_FAILED;
 			return false;
 		}
 
 		BPF.getInstance()
 				.getBPFLogger()
 				.debug(TAG,
 						"C_BlockProcessor.validate()  ciphersuite "
 								+ locals.owner_cs_num());
 
 		if (Ciphersuite.destination_is_local_node(bundle, block)) { // yes,
 																	// this is
 																	// ours
 
 			p = (Ciphersuite_C3) Ciphersuite.find_suite(locals.owner_cs_num());
 
 			if (p != null) {
 				result = p.validate(bundle, block_list, block,
 						reception_reason, deletion_reason);
 				return result;
 			} else {
 				BPF.getInstance()
 						.getBPFLogger()
 						.error(TAG,
 								"block failed security validation C_BlockProcessor");
 				deletion_reason[0] = BundleProtocol.status_report_reason_t.REASON_SECURITY_FAILED;
 				return false;
 			}
 		} else {
 			// not for here so we didn't check this block
 			locals.set_proc_flag(Ciphersuite.proc_flags_t.CS_BLOCK_DID_NOT_FAIL
 					.getCode());
 		}
 
 		return true;
 	}
 
 	/**
 	 * First callback to generate blocks for the output pass. The function is
 	 * expected to initialize an appropriate BlockInfo structure in the given
 	 * BlockInfoVec.
 	 * 
 	 * The base class simply initializes an empty BlockInfo with the appropriate
 	 * owner_ pointer.
 	 */
 	public int prepare(final Bundle bundle, BlockInfoVec xmit_blocks,
 			final BlockInfo source, final Link link, BlockInfo.list_owner_t list) {
 		Ciphersuite p = null;
 		int result = BP_FAIL;
 		BP_Local_CS locals = null;
 		BP_Local_CS source_locals = null;
 
 		if (list == BlockInfo.list_owner_t.LIST_RECEIVED) {
 			assert (source != null);
 			short cs_flags = 0;
 
 			if (Ciphersuite.destination_is_local_node(bundle, source))
 				return BP_SUCCESS; // don't forward if it's for here
 
 			xmit_blocks.add(new BlockInfo(this, source));
 			BlockInfo bp = (xmit_blocks.back());
 			bp.set_eid_list(source.eid_list());
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"C_BlockProcessor.prepare() - forward received block len "
 									+ source.full_length());
 
 			if (Ciphersuite.CS_FAIL_IF_NULL(source.locals())) // CS_FAIL_IF_NULL
 			{
 				return BP_FAIL;
 			}
 
 			source_locals = (BP_Local_CS) (source.locals());
 
 			if (Ciphersuite.CS_FAIL_IF_NULL(source_locals)) // CS_FAIL_IF_NULL
 			{
 				return BP_FAIL;
 			}
 
 			bp.set_locals(new BP_Local_CS());
 			locals = (BP_Local_CS) (bp.locals());
 
 			if (Ciphersuite.CS_FAIL_IF_NULL(locals)) // CS_FAIL_IF_NULL
 			{
 				return BP_FAIL;
 			}
 
 			locals.set_owner_cs_num(source_locals.owner_cs_num());
 			cs_flags = source_locals.cs_flags();
 			locals.set_correlator(source_locals.correlator());
 			locals.set_list_owner(BlockInfo.list_owner_t.LIST_RECEIVED);
 
 			// copy security-src and -dest if they exist
 			if ((source_locals.cs_flags() & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE
 					.getCode()) > 0) {
 				assert (source_locals.security_src().length() > 0);
 				cs_flags |= Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE
 						.getCode();
 				locals.set_security_src(source_locals.security_src());
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								"C_BlockProcessor.prepare() add security_src EID "
 										+ source_locals.security_src());
 			}
 
 			if ((source_locals.cs_flags() & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST
 					.getCode()) > 0) {
 				assert (source_locals.security_dest().length() > 0);
 				cs_flags |= Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST
 						.getCode();
 				locals.set_security_dest(source_locals.security_dest());
 				BPF.getInstance()
 						.getBPFLogger()
 						.debug(TAG,
 								"C_BlockProcessor.prepare() add security_dest EID "
 										+ source_locals.security_dest());
 			}
 			locals.set_cs_flags(cs_flags);
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"C_BlockProcessor.prepare() - inserted block eid_list_count "
 									+ bp.eid_list().size());
 			result = BP_SUCCESS;
 		} else {
 			if (source != null) {
 				source_locals = (BP_Local_CS) (source.locals());
 
 				if (Ciphersuite.CS_FAIL_IF_NULL(source_locals)) // CS_FAIL_IF_NULL
 				{
 					return BP_FAIL;
 				}
 
 				p = Ciphersuite.find_suite(source_locals.owner_cs_num());
 				if (p != null) {
 					result = p.prepare(bundle, xmit_blocks, source, link, list);
 				} else {
 					BPF.getInstance()
 							.getBPFLogger()
 							.error(TAG,
 									"C_BlockProcessor.prepare() - ciphersuite "
 											+ source_locals.owner_cs_num()
 											+ " is missing");
 				}
 			} // no msg if "source" is null, as BundleProtocol calls all BPs
 				// that way once
 		}
 		return result;
 	}
 
 	/**
 	 * Second callback for transmitting a bundle. This pass should generate any
 	 * data for the block that does not depend on other blocks' contents. It
 	 * MUST add any EID references it needs by calling block.add_eid(), then
 	 * call generate_preamble(), which will add the EIDs to the primary block's
 	 * dictionary and write their offsets to this block's preamble.
 	 */
 	public int generate(final Bundle bundle, BlockInfoVec xmit_blocks,
 			BlockInfo block, final Link link, boolean last) {
 		Ciphersuite p = null;
 		int result = BP_FAIL;
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "C_BlockProcessor.generate()");
 		BP_Local_CS locals = (BP_Local_CS) (block.locals());
 
 		if (locals == null) // CS_fail if null
 			return BP_FAIL;
 
 		p = Ciphersuite.find_suite(locals.owner_cs_num());
 		if (p != null) { // true because owner_cs_num was initialized in
 						  // ciphersuite_c3.prepare
 		
 			result = p.generate(bundle, xmit_blocks, block, link, last);
 		} else {
 			// generate the preamble and copy the data.
 			int length = block.source().data_length();
 
 			generate_preamble(
 					xmit_blocks,
 					block,
 					BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK,
 					BundleProtocol.block_flag_t.BLOCK_FLAG_DISCARD_BUNDLE_ONERROR
 							.getCode()
 							| (last ? BundleProtocol.block_flag_t.BLOCK_FLAG_LAST_BLOCK
 									.getCode() : 0), length);
 
 			IByteBuffer contents = block.writable_contents();
 			contents = BufferHelper.reserve(contents, block.data_offset()
 					+ length);
 
 			BufferHelper.copy_data(contents, block.data_offset(), block
 					.source().contents(), block.source().data_offset(), length);
 
 			result = BP_SUCCESS;
 		}
 		return result;
 	}
 
 	/**
 	 * Third callback for transmitting a bundle. This pass should generate any
 	 * data (such as security signatures) for the block that may depend on other
 	 * blocks' contents.
 	 */
 	public int finalize(final Bundle bundle, BlockInfoVec xmit_blocks,
 			BlockInfo block, final Link link) {
 		Ciphersuite p = null;
 		int result = BP_FAIL;
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "C_BlockProcessor.finalize()");
 
 		BP_Local_CS locals = (BP_Local_CS) (block.locals());
 
 		if (Ciphersuite.CS_FAIL_IF_NULL(locals)) {
 			return BP_FAIL;
 		}
 
 		p = Ciphersuite.find_suite(locals.owner_cs_num());
 		if (p != null) {
 			result = p.finalize(bundle, xmit_blocks, block, link);
 		}
 		// If we are called then it means that the ciphersuite for this Bundle
 		// does not exist at this node. All the work was done in generate()
 		return result;
 
 	}
 
 	public C_BlockProcessor(bundle_block_type_t blockType) {
 		super(blockType);
 	}
 }
