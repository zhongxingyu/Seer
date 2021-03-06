 /**
  * 
  */
 package org.crosby.batchsig.test;
 
 import org.rice.crosby.batchsig.Message;
 import org.rice.crosby.historytree.generated.Serialization.TreeSigBlob;
 
 class MessageWrap implements Message {
 	final byte data[];
 	TreeSigBlob signature;
 	Boolean targetvalidity = null;
 	Object recipient;
 	Object author;
 	
 	public MessageWrap(int i) {
 		data = String.format("Foo%d",i).getBytes(); 
 		recipient = this;
 		author = getClass();
 	}
 
 	MessageWrap setRecipient(Object o) {
 		recipient = o;
 		return this;
 	}
 
 	MessageWrap setAuthor(Object o) {
 		author = o;
 		return this;
 	}
 	
 	@Override
 	public byte[] getData() {
 		return data;
 	}
 
 	@Override
 	public Object getRecipient() {
 		return recipient;
 	}
 
 	@Override
 	public TreeSigBlob getSignatureBlob() {
 		return this.signature;
 	}
 
 	@Override
 	public Object getAuthor() {
 		return author;
 	}
 
 	@Override
 	public void signatureResult(TreeSigBlob sig) {
 		System.out.format("Storing signature of '%s' with sig: {{%s}}\n" , new String(data) ,sig.toString());
 		this.signature = sig;
 	}
 
 	@Override
 	public void signatureValidity(boolean valid) {
 		TestSimpleQueue.assertEquals(targetvalidity.booleanValue(),valid);
 		targetvalidity = null;
 	}
 
 	public void wantValid() {targetvalidity = true;}
 	public void wantInValid() {targetvalidity = false;}
 }
