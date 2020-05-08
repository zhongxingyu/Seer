 package br.ufu.facom.network.dlontology.msg;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 public class CustomParser implements OWLParser{
 	//PatternsMessage
 	private static final byte[] openTag = "<Message".getBytes();
 	private static final byte[] closeTag = "</Message>".getBytes();
 	
 	public static int sequenceActual=0;
 	
 	public byte[] parse(Message message){
 		byte[] vlanBytes = new byte[] { (byte)0x81,0x0, (byte)((message.getVlan() >> 8) & 0xFF), (byte)message.getVlan() }; //VLAN
 		byte[] etherType= new byte[]{0x8, (byte)0x80}; //PT_IANA
 		
 		byte[] msgPt1 = "<Message vl=".getBytes();
 		
 		byte[] msgPt2 = (" rdf:about=\"#"+message.getLabel()+"\" fragmented="+message.isFragmented()+" sequence="+message.getSequence()+">"+
 			"<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"+
 			"<Source rdf:resource=\"#"+message.getSource()+"\"/>"+
             "<Destination rdf:resource=\"#"+message.getDestination()+"\"/>"+
             "<Payload rdf:string=\"").getBytes();
 		
 		//byte[] msgPt3 = Base64.encode(message.getPayload()).getBytes();
 		byte[] msgPt3 = message.getPayload();
 		
 		byte[] msgPt4 = "\"/></Message>".getBytes();
 		
 		byte[] buffer = new byte[vlanBytes.length + etherType.length + msgPt1.length + msgPt2.length + msgPt3.length + msgPt4.length];
 		System.arraycopy(msgPt1, 0, buffer, 0, msgPt1.length);
 		System.arraycopy(vlanBytes, 0, buffer, msgPt1.length, vlanBytes.length);
 		System.arraycopy(etherType, 0, buffer, msgPt1.length + vlanBytes.length, etherType.length);
 		System.arraycopy(msgPt2, 0, buffer, msgPt1.length + vlanBytes.length + etherType.length, msgPt2.length);
 		System.arraycopy(msgPt3, 0, buffer, msgPt1.length + vlanBytes.length + etherType.length + msgPt2.length, msgPt3.length);
 		System.arraycopy(msgPt4, 0, buffer, msgPt1.length + vlanBytes.length + etherType.length + msgPt2.length + msgPt3.length, msgPt4.length);
 
 		return buffer;
 	}
 
 	
 	public Message parseMessage(byte[] data){
 		int indexFound = KPM.indexOf(data,"fragmented=".getBytes(),0);
 		if(indexFound == -1)
 			return null;
 		int posInFrag = indexFound + "fragmented=".getBytes().length;
 		
 		int posFinFrag =  KPM.indexOf(data," sequence=".getBytes(),posInFrag);
 		
 		int posInSeq = posFinFrag + " sequence=".getBytes().length;
 		
 		int posFinSeq = KPM.indexOf(data,"><rdf:type rdf".getBytes(),posInSeq);
 		if(posFinSeq == -1)
 			return null;
 		
 		indexFound = KPM.indexOf(data,"<Source rdf:resource=\"#".getBytes(),posFinSeq);
 		if(indexFound == -1)
 			return null;
 		int posInSource = indexFound + "<Source rdf:resource=\"#".getBytes().length;
 		
 		int posFinSource = KPM.indexOf(data,"\"/><Destination rdf:resource=\"#".getBytes(),posInSource);
 		if(posFinSource == -1)
 			return null;
 		
 		int posInDestin = posFinSource + "\"/><Destination rdf:resource=\"#".getBytes().length;
 		int posFinDestin = KPM.indexOf(data,"\"/><Payload rdf:string=\"".getBytes(),posInSource);
 		if(posFinDestin == -1)
 			return null;
 		
 		int posInPayload = posFinDestin + "\"/><Payload rdf:string=\"".getBytes().length;
		int posFinPayload = KPM.indexOf(data,"\"/></Message>".getBytes(),posInPayload);
		if(posFinPayload == -1)
			return null;
 		
 		boolean fragmented = new String(Arrays.copyOfRange(data, posInFrag, posFinFrag)).equals("true");
 		String sequence = new String(Arrays.copyOfRange(data, posInSeq, posFinSeq));
 		String source = new String(Arrays.copyOfRange(data, posInSource, posFinSource));
 		String destin = new String(Arrays.copyOfRange(data, posInDestin, posFinDestin));
 		byte payload[] = Arrays.copyOfRange(data, posInPayload, posFinPayload);
 		
 		return new Message(source,destin,payload,fragmented,sequence);
 	}
 	
 	public static class KPM {
 	    /**
 	     * Search the data byte array for the first occurrence
 	     * of the byte array pattern.
 	     */
 	    public static int indexOf(byte[] data, byte[] pattern, int offset) {
 	        int[] failure = computeFailure(pattern);
 
 	        int j = 0;
 
 	        for (int i = offset; i < data.length; i++) {
 	            while (j > 0 && pattern[j] != data[i]) {
 	                j = failure[j - 1];
 	            }
 	            if (pattern[j] == data[i]) {
 	                j++;
 	            }
 	            if (j == pattern.length) {
 	                return i - pattern.length + 1;
 	            }
 	        }
 	        return -1;
 	    }
 
 	    /**
 	     * Computes the failure function using a boot-strapping process,
 	     * where the pattern is matched against itself.
 	     */
 	    private static int[] computeFailure(byte[] pattern) {
 	        int[] failure = new int[pattern.length];
 
 	        int j = 0;
 	        for (int i = 1; i < pattern.length; i++) {
 	            while (j>0 && pattern[j] != pattern[i]) {
 	                j = failure[j - 1];
 	            }
 	            if (pattern[j] == pattern[i]) {
 	                j++;
 	            }
 	            failure[i] = j;
 	        }
 
 	        return failure;
 	    }
 	}
 
 
 	@Override
 	public boolean validStartMessage(byte[] bytes) {
 		int sizeOpenTag = openTag.length;
 		int sizeBytes = bytes.length;
 		
 		if(sizeBytes >= sizeOpenTag){
 			for(int i=0; i<sizeOpenTag; i++){
 				if(openTag[i] != bytes[i])
 					return false;
 			}
 			return true;
 		}else
 			return false;
 	}
 
 	@Override
 	public boolean validEndMessage(byte[] bytes,int offset) {
 		int sizeOpenTag = openTag.length;
 		int sizeBytes = offset;
 		int sizeCloseTag = closeTag.length;
 		
 		if(sizeBytes >= (sizeOpenTag + sizeCloseTag)){
 			for(int i=0; i<sizeCloseTag; i++){
 				int indexBytes = sizeBytes - 1 - i;
 				int indexClose = sizeCloseTag - 1 - i;
 
 				if(closeTag[indexClose] != bytes[indexBytes])
 					return false;
 			}
 			return true;
 		}else
 			return false;
 	}
 
 
 	@Override
 	public List<Message> fragmentMessage(String title, int vlan, String destin, byte[] msg) {
 		int overhead=202;
 		int fields = title.getBytes().length + destin.getBytes().length + "FinMessage".getBytes().length + 4 + 8 + (""+(sequenceActual++)).length();//fragmented + vlan
 		
 		List<Message> list = new ArrayList<Message>();
 		int offset=0;
 		int size = msg.length;
 		
 		int pos = 0;
 		while((overhead+fields+(size-offset))+(""+pos).length() > Message.MAX_FRAME_SIZE){
 			int parcial = Message.MAX_FRAME_SIZE - (overhead+fields) + 1 - ((""+pos).length());
 			
 			list.add(new Message(title, destin, vlan, Arrays.copyOfRange(msg, offset, offset+parcial), true,sequenceActual+"."+(pos++)));
 			
 			offset += parcial;
 		}
 		
 		String seqStr = ""+sequenceActual;
 		if(pos>0)
 			seqStr+="."+pos;
 			
 			list.add(new Message(title, destin, vlan, Arrays.copyOfRange(msg, offset, size),false,seqStr));
 
 		return list;
 	}
 	
 	public static void  main(String...args){
 		System.out.println(" sequence=".getBytes().length);
 	}
 	
 }
