 package gov.usgs.cida.ncetl.sis;
 
 
 import gov.usgs.cida.ncetl.jpa.ArchiveConfig;
 import gov.usgs.cida.ncetl.jpa.ConfigFetcherI;
 
 import org.springframework.integration.Message;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.integration.support.MessageBuilder;
 
 
 /** Add ETL configuration to message headers **/
 public class ConfigEnhancer {
 
 	private gov.usgs.cida.ncetl.jpa.ConfigFetcherI asm;
 	
 	@Transformer
 	public Message<?> getConfig(Message<?> msg) {
 		Integer rfc = msg.getHeaders().get("rfc",  Integer.class);
 		
 		ArchiveConfig spec = asm.fetch(rfc);
 		
 		MessageBuilder<?> mb = MessageBuilder.fromMessage(msg);
 		mb.setHeader("spec", spec);
 		return mb.build();
 	}
 
	public void setConfigFetcher(gov.usgs.cida.ncetl.jpa.ConfigFetcherI asm) {
 		this.asm = asm;
 	}
 	
 	
 }
