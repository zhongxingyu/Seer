 package se.kth.ssvl.tslab.wsn.general.servlib.security;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfo;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.blocks.BlockInfoVec;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.contacts.links.Link;
 
 /**
  * TODO
  * 
  */
 
 public class Security {
 	
 	private static String TAG = "Security";
 	
 	public Security(){
 		Ciphersuite.init_default_ciphersuites();
 
 		BPF.getInstance().getBPFLogger().debug(TAG, "Initialized.");
 	}
 	
 	// This method will be called from BundleProtocol.prepare_blocks()
 	public static void prepare_out_blocks(Bundle bundle, Link link, BlockInfoVec xmit_blocks) {
 		int err = 0;
 		
 		//Do we have to use PCB?
 		if(BPF.getInstance().getConfig().security_setting().use_pcb()) {
 			Ciphersuite bp = Ciphersuite.find_suite(Ciphersuite_C3.CSNUM_C3);
 			assert (bp != null);
 			err = bp.prepare(bundle, xmit_blocks, null, link,
 					BlockInfo.list_owner_t.LIST_NONE);
 		}
 
 		// Do we have to use PIB?
 		if(BPF.getInstance().getConfig().security_setting().use_pib()) {
			BPF.getInstance().getBPFLogger().error(TAG, "PIB blocks are not supported yet!");
 			// TODO: Add PIB support!
 		}
 
 		if (err != 0) {
 			BPF.getInstance().getBPFLogger().error(TAG, "Error while preparing security blocks!");
 		} else {
 			BPF.getInstance().getBPFLogger().debug(TAG, "Blocks have been prepared.");
 		}
 	}
 	
 }
