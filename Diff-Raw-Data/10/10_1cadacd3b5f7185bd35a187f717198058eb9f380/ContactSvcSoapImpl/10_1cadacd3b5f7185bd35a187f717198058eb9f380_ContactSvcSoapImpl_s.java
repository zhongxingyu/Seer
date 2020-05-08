 /**
  * 
  */
 package fr.axxx.pivotal;
 
 import java.math.BigDecimal;
import javax.jws.WebService;
 import org.osoa.sca.annotations.Reference;
 import org.osoa.sca.annotations.Scope;
 import fr.axxx.pivotal.client.api.ClientService;
 import fr.axxx.pivotal.client.model.Client;
 import fr.axxx.pivotal.client.model.InformationAPV;
 import fr.axxx.pivotal.client.model.ContactClient;
 
 /**
  * @author jguillemotte
  *
  */
 @Scope("COMPOSITE")
 public class ContactSvcSoapImpl implements ContactSvcSoap {
 
     @Reference
     private ClientService clientService;
     
     @Override
     public ArrayOfString client(String identifiantClient, String raisonSociale, Integer anciennete, String typeStructure, String numEtVoie, String email, String codePostal, String ville, String pays,
             String tel, String rib, String formeJuridique, String siren, BigDecimal dotGlobAPVN, BigDecimal dontReliquatN1, BigDecimal dontDotN, BigDecimal nbBenefPrevN, BigDecimal montantUtiliseN,
             BigDecimal nbBenefN) {
         
         ArrayOfString arrayOfString = new ArrayOfString();
         // TODO To complete
         Client client = clientService.createClient(identifiantClient, raisonSociale, siren, email);
        arrayOfString.string.add(client.getIdentifiantClient());
         return arrayOfString;
     }
 
     @Override
     public ArrayOfString informationAPV(String identifiantClient, String bilanLibelle, Integer nombre, Integer bilanAnnee) {
         ArrayOfString arrayOfString = new ArrayOfString();
         // TODO To complete
         InformationAPV informationApv = clientService.createInformationApv(identifiantClient, bilanLibelle, nombre, bilanAnnee);
        arrayOfString.string.add(informationApv.getIdentifiantClient());
         return arrayOfString;
     }
 
     @Override
     public ArrayOfString contactClient(String identifiantClient, String nomContact, String prenomContact, String fonctionContact, String telephone, String email, String numEtVoie, String codePostal,
             String ville, String pays) {
         ArrayOfString arrayOfString = new ArrayOfString();
         // TODO To complete
         ContactClient contactClient = clientService.createContactClient(identifiantClient, nomContact, prenomContact, fonctionContact, telephone, email, numEtVoie, codePostal, ville, pays);
        arrayOfString.string.add(contactClient.getIdentifiantClient());
         return arrayOfString;
     }
 
 }
