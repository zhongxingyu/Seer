 package br.gov.frameworkdemoiselle.spatial.sample.commom.business;
 
 import javax.inject.Inject;
 
 import br.gov.frameworkdemoiselle.annotation.Startup;
 import br.gov.frameworkdemoiselle.message.MessageContext;
 import br.gov.frameworkdemoiselle.spatial.sample.commom.domain.Client;
 import br.gov.frameworkdemoiselle.spatial.sample.commom.message.InfoMessages;
 import br.gov.frameworkdemoiselle.spatial.sample.commom.persistence.ClientDAO;
 import br.gov.frameworkdemoiselle.template.DelegateCrud;
 import br.gov.frameworkdemoiselle.transaction.Transactional;
 
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKTReader;
 
 public class ClientBC extends DelegateCrud<Client, Long, ClientDAO> {
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Inject
     private MessageContext messageContext;
 	

 	
 
 
 	@Override
 	public void delete(Long id) {
 		super.delete(id);
 		
 		 messageContext.add(InfoMessages.CLIENT_DELETE_OK,id);
 	}
 
 
 	@Override
 	public void insert(Client bean) {
		

		
 		super.insert(bean);
 		
 		 messageContext.add(InfoMessages.CLIENT_INSERT_OK, bean.getName());
 	}
 
 
 	@Override
 	public void update(Client bean) {
 		super.update(bean);
 		
 		 messageContext.add(InfoMessages.CLIENT_UPDATE_OK,bean.getName());
 	}
 	
 	@Startup
 	@Transactional
 	public void load() {
 		if (findAll().isEmpty()) {
 			try {
 				insert(new Client("Rafael Soto", "2021-9833", "rafael.soto@serpro.gov.br", new WKTReader().read("POINT(-38.48760780000001 -12.9710208)")));
 				insert(new Client("Asdrubal da Silva", "4861-1313", "asdrubal@jetsync.com", new WKTReader().read("POINT(133.59375 -25.3125)")));
 				insert(new Client("Fulano Pereira", "3245-9874", "fulano@recopany.com", new WKTReader().read("POINT(-73.125 40.078125)")));
 				insert(new Client("Sicrano Fernandez", "3245-9874", "sicrano.fernandez@recopany.com", new WKTReader().read("POINT(-5.625 37.96875)")));
 			} catch (ParseException e) {				
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
