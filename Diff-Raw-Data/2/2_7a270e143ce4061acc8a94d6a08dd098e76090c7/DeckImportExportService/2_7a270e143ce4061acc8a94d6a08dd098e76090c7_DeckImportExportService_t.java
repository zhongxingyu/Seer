 package de.uniwue.jpp.deckgen.io;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
  
 import de.uniwue.jpp.deckgen.model.Deck;
 import de.uniwue.jpp.deckgen.model.ICard;
 import de.uniwue.jpp.deckgen.model.objectValidator;
 import de.uniwue.jpp.deckgen.repository.ICardRepository;
 
 import org.w3c.dom.*;
 
 
 public class DeckImportExportService {
 
 	ICardRepository cardRepository;
 	
 	public DeckImportExportService() {
 		
 	}
 	public DeckImportExportService(ICardRepository cardRepository){
 		objectValidator.validate(cardRepository);
 		this.cardRepository = cardRepository;
 		
 	}
 	
 	public Set<Deck> importFrom(InputStream in) throws ImportException{
 		System.out.println("DeckImport gestartet");
 		Set<Deck> decks = new HashSet<Deck>();
 		Document doc;
 		XPath xp;
 		Element rootElement;
 		
 		try{
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
 			doc = docBuilder.parse(in);
 			rootElement = doc.getDocumentElement();
  
 			XPathFactory xpf = XPathFactory.newInstance();
 			xp = xpf.newXPath();
 		
 			String title="check",comment, cardName;
 			comment = cardName = "";
 			int count=1;
 			while(!title.isEmpty()){
 				
 				String xPath = "//deck["+count+"]/";
 				title = xp.evaluate(xPath+"title", rootElement);
 				if(!title.isEmpty()){
 					comment = xp.evaluate(xPath+"comment",rootElement);
 					Set<ICard> tempCards = new HashSet<ICard>();
 					XPathExpression expr = xp.compile("//deck["+count+"]/cards");
 					NodeList cardList = (((NodeList) expr.evaluate(doc,XPathConstants.NODESET)).item(0)).getChildNodes();
 					
 					if(cardList.getLength()==21){
 						for(int i=0;i<21;i++){
 							Node tempNode = cardList.item(i);
 							if(tempNode.getNodeType() == 1){
 							Element tempCardElement = (Element)tempNode;
 							cardName = tempCardElement.getAttribute("name");
 							ICard tempCard = cardRepository.find(cardName);
 							objectValidator.validate(tempCard);
 							tempCards.add(tempCard);
 							}
 						}
 						if(tempCards!=null)
 						{
 							Deck tempDeck = new Deck(title, comment, tempCards);
 							decks.add(tempDeck);
 						}
 					}
 					else
 						System.out.println("CardList.length: " + cardList.getLength());
 				}
 				count++;
 			}
 			System.out.println("DeckImport beendet");
 			
 			return decks;
 			
 		
 		}
 		catch(Exception e){
 			System.out.println("Fehler in DeckImport: " + e.getMessage());
 			e.printStackTrace();
 			throw new ImportException();
 		}
 		
 
 	}
 	public void exportTo(Set<Deck> decks, OutputStream out)throws ExportException{
 		System.out.println("DeckExport gestartet");
 		
 		DocumentBuilderFactory docFactory;
 		DocumentBuilder docBuilder;
 		Document doc;
 		try{
 			docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 			doc = docBuilder.newDocument();
 		}
 		catch(Exception e) {
 			System.out.println("Fehler: " + e.getMessage());
 			throw new ExportException();
 		}
 		Element rootElement = doc.createElement("decks");
 		doc.appendChild(rootElement);
 		
 		Iterator<Deck> myIterator = decks.iterator();
 		while(myIterator.hasNext()){
 			Deck tempDeck = myIterator.next();
 			Element tempDeckElement = doc.createElement("deck");
 			rootElement.appendChild(tempDeckElement);
 			
			Element tempNameElement = doc.createElement("title");
 			tempNameElement.appendChild(doc.createTextNode(tempDeck.getTitle()));
 			tempDeckElement.appendChild(tempNameElement);
 			
 			Element tempCommentElement = doc.createElement("comment");
 			tempCommentElement.appendChild(doc.createTextNode(tempDeck.getComment()));
 			tempDeckElement.appendChild(tempCommentElement);
 			
 			Element tempCardsParentElement = doc.createElement("cards");
 			tempDeckElement.appendChild(tempCardsParentElement);
 			
 			for(ICard tempCard : tempDeck.getCards()){
 				Element tempCardElement = doc.createElement("card");
 				tempCardElement.setAttribute("name",tempCard.getName());
 				tempCardsParentElement.appendChild(tempCardElement);
 			}
 		}
 		try{
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(source, new StreamResult(out));
 			
 		}
 		catch(Exception e) {
 			System.out.println("Fehler in der Streamausgabe: " + e.getMessage());
 			throw new ExportException();
 		}
 		System.out.println("DeckExport beendet");
 		
 	}
 	
 }
