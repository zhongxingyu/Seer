 package se.kth.ssvl.tslab.wsn.general.servlib.routing.routers;
 
import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.ForwardingInfo;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle.priority_values_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleActions;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleDaemon;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleList;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundlePayload.location_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol.status_report_reason_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.ExpirationTimer;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.custody.CustodyTimerSpec;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleDeleteRequest;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleDeliveredEvent;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleReceivedEvent;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.ContactUpEvent;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.event_source_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.contacts.links.Link;
 import se.kth.ssvl.tslab.wsn.general.servlib.naming.endpoint.EndpointID;
 import se.kth.ssvl.tslab.wsn.general.servlib.naming.endpoint.EndpointIDPattern;
 import se.kth.ssvl.tslab.wsn.general.servlib.reg.EpidemicRegistration;
 import se.kth.ssvl.tslab.wsn.general.servlib.reg.Registration;
 import se.kth.ssvl.tslab.wsn.general.servlib.routing.routerentry.RouteEntry;
 import se.kth.ssvl.tslab.wsn.general.servlib.routing.routerentry.RouteEntryVec;
 import se.kth.ssvl.tslab.wsn.general.servlib.storage.BundleStore;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.IByteBuffer;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.SerializableByteBuffer;
 
 public class EpidemicBundleRouter extends TableBasedRouter {
 
 	private final static String TAG = "EpidemicBundleRouter";
 
 	private EpidemicRegistration registration = new EpidemicRegistration(this);
 	
 	public EpidemicBundleRouter(BundleActions actions, BundleList pendingBundles, BundleList custodyBundles) {
 		super(actions, pendingBundles, custodyBundles);
 	}
 	
 	@Override
 	public Registration getRegistration() {
 		return registration;
 	}
 
 	protected void handle_contact_up(ContactUpEvent event) {
 		Link link = event.contact().link();
 		sendList(link);
 	}
 
 	public void sendList(Link link) {
 
 		String list[] = BundleStore.getInstance().getHashList();
 		if(list == null){
 			BPF.getInstance().getBPFLogger().warning(TAG, "List is empty, sending empty list");
 			list = new String[]{"empty"};
 		}
 		BPF.getInstance().getBPFLogger().debug(TAG, "size of hashlist: " + list.length);
 		
 		// "check if the link is not open
 		if (!link.isopen()) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.warning(
 							TAG,
 							"Trying to send list but link " + link.name()
 									+ " is not open!");
 			return;
 		}
 
 		// check if the link queue doesn't have space
 		if (!link.queue_has_space()) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.warning(
 							TAG,
 							"Trying to send list but link " + link.name()
 									+ " does not have space in queue!");
 			return;
 		}
 
 		// create bundle containing list
 		Bundle bundle = new Bundle(location_t.MEMORY);
 		bundle.set_dest(new EndpointID(link.remote_eid() + "/epidemic"));
 		bundle.set_source(new EndpointID(BundleDaemon.getInstance().local_eid().str() + "/epidemic"));
 		bundle.set_prevhop(BundleDaemon.getInstance().local_eid());
 		bundle.set_custodian(EndpointID.NULL_EID());
 		bundle.set_replyto(new EndpointID(BundleDaemon.getInstance().local_eid().str() + "/epidemic"));
 		bundle.set_singleton_dest(true);
 		bundle.set_expiration(60000);
 		bundle.set_expiration_timer(new ExpirationTimer(bundle));
 		bundle.set_priority(priority_values_t.COS_EXPEDITED);
 
 		//format the payload -> [hash1]-[hash2]-[hash3]...
 		String payload = "";
 		for (int i = 0; i < list.length; i++) {
 			payload += list[i];
 			if (i != list.length - 1) {
 				payload += "-";
 			}
 		}
 		BPF.getInstance().getBPFLogger().info(TAG, "List payload: " + payload);
 		bundle.payload().set_data(payload.getBytes());
 
 		
 		ForwardingInfo info = new ForwardingInfo(ForwardingInfo.state_t.NONE,
 				ForwardingInfo.action_t.FORWARD_ACTION, link.name_str(),
 				0xffffffff, link.remote_eid(),
 				CustodyTimerSpec.getDefaultInstance());
 
 		// send bundle
 		actions_.queue_bundle(bundle, link, info.action(), info.custody_spec());
 		BPF.getInstance().getBPFLogger().debug(TAG, "Trying to send Epidemic List with payload: " + payload);
 	}
 
 	public void deliver_bundle(Bundle bundle, Link link) {
		IByteBuffer buf = new SerializableByteBuffer(bundle.payload().length());
 
 		if (!bundle.payload().read_data(0, bundle.payload().length(), buf)) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Couldn't read the epidemic bundle");
 			return;
 		}
 		BPF.getInstance()
 				.getBPFLogger()
 				.info(TAG,
 						"Epidemic bundle received. List: "
 								+ new String(buf.array()));
 		
 		// Split the list on dashes (hashes and dashes!)
		String[] neighborList = new String(buf.array(), 0, bundle.payload().length()).split("-");
 		
 		String remote_eid = bundle.source().str().split("/epidemic")[0];
 		if (remote_eid == null) {
 			BPF.getInstance().getBPFLogger()
 					.error("TAG", "Bundle recv : remote_eid == null");
 			return;
 		}
 
 		String[] local = BundleStore.getInstance().getHashList();
 		if (local == null) {
 			BPF.getInstance().getBPFLogger().info(TAG,
 							"Local list is empty! Not checking for diffs.");
 		} else {
 			String[] diff = diff(local, neighborList);
 			BPF.getInstance().getBPFLogger()
 					.info(TAG, "Diff is: " + Arrays.toString(diff));
 			for (int i = 0; i < diff.length; i++) {
 				// get bundle given hash
 				Bundle b = BundleStore.getInstance().getBundle(diff[i]);
 				BPF.getInstance().getBPFLogger().debug(TAG,
 								"Trying to send bundle with hash: " + diff[i]);
 				// queue bundle
 				ForwardingInfo info = new ForwardingInfo(ForwardingInfo.state_t.NONE,
 						ForwardingInfo.action_t.FORWARD_ACTION, link.name_str(),
 						0xffffffff, link.remote_eid(),
 						CustodyTimerSpec.getDefaultInstance());
 
 				// send bundle
 				actions_.queue_bundle(b, link, info.action(), info.custody_spec());
 //				BundleDaemon.getInstance().post(
 //						new BundleDeleteRequest(b, status_report_reason_t.REASON_NO_ADDTL_INFO));
 			}
 		}
 	}
 	
 	
 	protected int route_bundle(Bundle bundle) {
 		
 		BPF.getInstance().getBPFLogger().info(TAG,
 				"route_bundle has been called but we are not doing anything");
 		
 		return 0;
 	}
 	
 	/**
 	 * Get the items that are not in the remote list compared to our local
 	 * @param local Our local list
 	 * @param remote The remote list to compare to
 	 * @return The diff list
 	 */
 	private String[] diff(String[] local, String[] remote) {
 		if (local == null || remote == null) {
 			return new String[0];
 		}
 		
 		ArrayList<String> res = new ArrayList<String>();
		boolean foundInRemote = false;
 		for (int i = 0; i < local.length; i++) {
 			for (int j = 0; j < remote.length; j++) {
				if (local[i].contains(remote[j])) {
					foundInRemote = true;
 				}
 			}
			
			// If we found it in the remote, then don't add it to the diff
			if (!foundInRemote) {
				res.add(local[i]);
			} else {
				foundInRemote = false;
			}
 		}
 		
 		return res.toArray(new String[res.size()]);
 	}
}
