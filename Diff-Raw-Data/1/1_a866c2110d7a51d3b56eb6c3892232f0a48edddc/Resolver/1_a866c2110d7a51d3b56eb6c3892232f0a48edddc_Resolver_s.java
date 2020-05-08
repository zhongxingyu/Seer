 package nl.sidn.dnslib.logic;
 
 import nl.sidn.dnslib.logic.unbound.UnboundLibrary;
 import nl.sidn.dnslib.logic.unbound.ub_result;
 import nl.sidn.dnslib.message.Message;
 import nl.sidn.dnslib.message.util.NetworkData;
 import nl.sidn.dnslib.types.RcodeType;
 import nl.sidn.dnslib.types.ResourceRecordClass;
 import nl.sidn.dnslib.types.ResourceRecordType;
 
 import org.bridj.Pointer;
 
 public class Resolver {
 	
 	private Pointer<UnboundLibrary.ub_ctx> ctx;
 		
 	public Resolver(){
 		ctx = UnboundLibrary.ub_ctx_create();
 		//start with iterator only
 		ResolverContextBuilder builder =  new ResolverContextBuilder();
 		ctx = builder.withIterator().build().getCtx();
 		
 	}
 	
 	public Resolver(Context ctx){
 		this.ctx = ctx.getCtx();
 	}
 	
 
 	public LookupResult lookup(String qName, ResourceRecordType qType, ResourceRecordClass qClazz, boolean decode ){
 
 		Pointer<Byte> name = Pointer.pointerToCString(qName);
 
 		Pointer<Pointer<ub_result>> result = Pointer.allocatePointer(ub_result.class);
 		int status = UnboundLibrary.ub_resolve(ctx, name, qType.getValue(), qClazz.getValue(), result);
 				
 		//UnboundLibrary.ub_ctx_delete(ctx);
 		 
 		LookupResult lr = new LookupResult();
 		if(status == 0){
 			/* lookup was successful, get the result */
 			ub_result ubr = result.get().get();
 				
 			lr.setHaveData(ubr.havedata() == 0? false: true);
 			lr.setBogus(ubr.bogus() == 0? false: true);
 			lr.setNxDomain(ubr.nxdomain() == 0? false: true);
 			lr.setqName(ubr.qname().getCString());
 			lr.setqType(ResourceRecordType.fromValue(ubr.qtype()));
 			lr.setqClazz(ResourceRecordClass.fromValue(ubr.qclass()));		
 			lr.setRcode(RcodeType.fromValue(ubr.rcode()));			
 			lr.setDatapacketLength(ubr.answer_len());
 		    /* only get packetdata if any data is available */
 			if(lr.getDatapacketLength() > 0){
 				//set the raw bytes
 				lr.setDatapacket(ubr.answer_packet().getBytes(lr.getDatapacketLength()));
 				//if requested decode all the packet bytes
 				if(decode){
 					NetworkData nd = new NetworkData(lr.getDatapacket());
 					Message msg = new Message();
 					msg.decode(nd);
 					lr.setPacket(msg);
 				}
 			}
 			
 			if(lr.isHaveData()){
 				if(ubr.canonname() != null){
 					lr.setCanconname(ubr.canonname().getCString());
 				}
 			}
 			
 			lr.setSecure(ubr.secure() == 0? false: true);
 			lr.setBogus(ubr.bogus() == 0? false: true);
 			if(lr.isBogus()){
 				lr.setWhyBogus(ubr.why_bogus().getCString());
 			}
 			return lr;
 		}else{
 			/* lookup failed, get the error message from libunbound */
 			Pointer<Byte> errorString = UnboundLibrary.ub_strerror(status);
 			errorString.getCString();
 			lr.setStatus(errorString.getCString());
 		}
 		
 		return lr;
 		
 	}
 	
 	public void cleanup(){
 		if(ctx != null){
 			UnboundLibrary.ub_ctx_delete(ctx);
 			ctx = null;
 		}
 	}
 }
