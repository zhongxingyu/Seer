 /*
  * XmlPackageSessionTest.java
  *
  * Created on 25 de marzo de 2003, 07:03 PM
  */
 
 package py.edu.uca.fcyt.toluca.net;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 import py.edu.uca.fcyt.game.ChatPanelContainer;
 import py.edu.uca.fcyt.net.XmlPackagesSession;
 import py.edu.uca.fcyt.toluca.RoomClient;
 import py.edu.uca.fcyt.toluca.event.RoomEvent;
 import py.edu.uca.fcyt.toluca.event.TableEvent;
 import py.edu.uca.fcyt.toluca.event.TrucoEvent;
 import py.edu.uca.fcyt.toluca.game.TrucoCard;
 import py.edu.uca.fcyt.toluca.game.TrucoGameClient;
 import py.edu.uca.fcyt.toluca.game.TrucoPlay;
 import py.edu.uca.fcyt.toluca.game.TrucoPlayer;
 import py.edu.uca.fcyt.toluca.game.TrucoTeam;
 import py.edu.uca.fcyt.toluca.table.Table;
 /**
  *
  * @author  PABLO JAVIER
  */
 public class CommunicatorClient extends Communicator
 {
 	
 	RoomClient pieza;
 	TrucoTeam equipo1;
 	TrucoTeam equipo2;
 	byte kindCardAux;
 	byte valueCardAux;
 	private TrucoPlayer mi_jugador = null;
 	// Este es el que hay que usar como referencia al jugador representado!!!
 	/** Creates a new instance of XmlPackageSessionTest */
 	ChatPanelContainer Chatpanel;
 	
 	/** Holds value of property player. */
 	private TrucoPlayer player;
 	
 	public CommunicatorClient(RoomClient pieza)
 	{
 		super();
 		this.pieza = pieza;
 		int retinit = init();
 		System.out.println(getInitErrorMessage(retinit));
 		pieza.addRoomListener(this);
 	}
 	
 	public CommunicatorClient()
 	{
 		super();
 		int retinit = init();
 		System.out.println(getInitErrorMessage(retinit));
 	}
 	public String getInitErrorMessage(int errcode)
 	{
 		return "Sin errores";
 	}
 	
 	public int init()
 	{
 		int ret = -1;
 		try
 		{
 			//setSocket(new Socket("interno.roshka.com.py", 6767));
			//setSocket(new Socket("ray-ray.roshka.com.py", 6767));
 			setSocket(new Socket("ray-ray.roshka.com.py", 6767));
 			ret = XmlPackagesSession.XML_PACKAGE_SESSION_INIT_OK;
 		} catch (UnknownHostException e)
 		{
 			ret = -5;
 		} catch (IOException e)
 		{
 			ret = -4;
 		}
 		return ret;
 	}
 	
 	public void receiveXmlPackage(Element xmlPackage)
 	{
 		System.out.println("//////////////////////////////////////////");
 		System.out.println("Vino un paquete: " + xmlPackage.getName());
 		Document doc = new Document(xmlPackage);
 		try
 		{
 			
 			XMLOutputter serializer = new XMLOutputter("  ", true);
 			
 			serializer.output(doc, System.out);
 			
 		} catch (IOException e)
 		{
 			System.out.println(e);
 		}
 		System.out.println("//////////////////////////////////////////");
 		cabecera(doc);
 		//chat.agregar(xmlPackage.getChild("Usuario").getText() + ": " + xmlPackage.getChild("Mensaje").getText());
 		
 	}
 	
 	public void sendXmlPackage(String option, RoomEvent te)
 	{
 		new Throwable("Nada implementado aun  :-( ").printStackTrace(System.out);
 	}
 	
 	public void receiveXmlPackageWithParsingError(String rawXmlPackage)
 	{
 		System.out.println("El XML tiene errores:\n" + rawXmlPackage);
 	}
 	
 	public void loginRequested(RoomEvent te)
 	{
 		//peticion de loginc
 		Document doc;
 		doc = super.xmlCreateLogin(te);
 				/*try {
 				 
 					XMLOutputter serializer = new XMLOutputter("  ", true);
 				 
 					serializer.output(doc, System.out);
 				 
 				}
 				catch (IOException e) {
 					System.out.println(e);
 				}*/
 		
 		super.sendXmlPackage(doc);
 	}
 	public void chatMessageRequested(
 	ChatPanelContainer chatPanelContainer,
 	TrucoPlayer jug,
 	String htmlMessage)
 	{
 		Document doc;
 		//this.ChatPanel=panel;
 		doc = super.xmlCreateChatMsg(chatPanelContainer, jug, htmlMessage);
 		super.sendXmlPackage(doc);
 	}
 	
 	public void playerLeft(TableEvent te)
 	{
 		Document doc = te.toXml();
 		sendXmlPackage(doc);
 	}
 	
 
 	public void xmlReadChatMsg(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				user = element.getAttributeValue("name");
 			}
 			if (aux.compareTo("Msg") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				message = element.getText();
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadChatMsg(child);
 			}
 			if (aux.compareTo("ChatMsg") == 0)
 			{
 				//Chatpanel.showChatMessage(user,message);
 				System.out.println("Player: " + user);
 				System.out.println("Mensaje: " + message);
 				// Aca vemos para quin puta es el mensaje
 				String origen = element.getAttributeValue("origin");
 				System.out.println("El origen es: " + origen);
 				if (origen.equalsIgnoreCase("room"))
 				{
 				
 					//pieza.showChatMessage(new TrucoPlayer(user, 0), message);
 					pieza.showChatMessage(pieza.getPlayer(user),message);
 					
 				} else
 				{
 					//Table t = (Table) getTables().get(origen);
 					Table t = (Table)(pieza.getHashTable().get(new Integer(origen)));
 					if (t != null)
 					{
 						//t.showChatMessage(new TrucoPlayer(user, 0), message);
 						t.showChatMessage(pieza.getPlayer(user),message);
 					} else
 					
 					{
 						System.out.println(
 						"NO PUDIMOS ENTREGAR EL CHAT!!! !@@!#$#@@Q");
 					}
 				}
 			}
 		}
 	}
 	
 	public void tableCreated(RoomEvent re)
 	{
 		// Problemas de diseno a resolver en TolucaV2
 		
 	}
 	int typeAux;
 	int tantoAux;
 	public void xmlReadCantarTanto(Object o)
 	{
 		String aux;
 		
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				typeAux = Integer.parseInt(element.getAttributeValue("id"));
 				
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 				
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				handAux = Integer.parseInt(element.getAttributeValue("number"));
 				
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 				
 			}
 			if (aux.compareTo("Tanto") == 0)
 			{
 				tantoAux = Integer.parseInt(element.getAttributeValue("tanto"));
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				
 				xmlReadCantarTanto(child);
 			}
 			if (aux.compareTo("CantarTanto") == 0)
 			{
 				System.out.println("quieron cantar mi tanto!!");
 				
 				//TrucoGameClient myCliente = (TrucoGameClient)((Table)getTables().get(String.valueOf(gameID))).getTGame();
 				TrucoGameClient myCliente = (TrucoGameClient)((Table)pieza.getHashTable().get(new Integer(gameID))).getTGame();
 				TrucoPlayer myPlayer = pieza.getPlayer(userAux);
 				
 				int myValue = tantoAux;
 				TrucoEvent ev = new TrucoEvent(myCliente ,handAux,myPlayer,(byte)typeAux,tantoAux);
 				TrucoPlay tp = ev.toTrucoPlay();
 				if (getAssociatedPlayer() != myPlayer)
 				{
 					System.out.println("Ejecuto primero la jugada en el cliente!!!: " + tp.getPlayer().getName()+"jugada"+tp.getType());
 					myCliente.play(tp);
 				}
 				myCliente.playResponse(myPlayer,ev.getTypeEvent(),tantoAux);
 				System.out.println("se cantou tanto, furou?");
 			}
 		}
 	}
 	
 	
 	int valueAux;
 	public void xmlReadTrucoPlay(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				typeAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 			}
 			if (aux.compareTo("Carta") == 0)
 			{
 				String kind = element.getAttributeValue("kind");
 				String value = element.getAttributeValue("value");
                 cardAux = new TrucoCard(
 				Integer.parseInt(kind),
 				Integer.parseInt(value)); //No se porq estaba comentado, me daba un null pointer x eso descomente
 			}
 			if (aux.compareTo("Value") == 0)
 			{
 				
 				valueAux = Integer.parseInt(element.getAttributeValue("val"));
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTrucoPlay(child);
 			}
 			if (aux.compareTo("TrucoPlay") == 0)
 			{
 				//CUANDO SE CREA ESTE TRUCOPLAY HAY QUE PASARLE LA REFENCIA AL PLAYER CORRESPONDIENTE
 				//CREEMOS QUE PODEMOS SACAR DE LA TABLA PERO HAY QUE VER
 				TrucoPlay tp =
 				new TrucoPlay(
 				tableIdAux,
 				new TrucoPlayer(userAux),
 				(byte) typeAux,
 				cardAux,
 				valueAux);
 				
 				System.out.println("Tabla :" + tp.getTableNumber());
 				System.out.println(
 				"Truco Player : " + tp.getPlayer().getName());
 				System.out.println("TYPE  : " + tp.getType());
 				System.out.println(
 				"LA CARTA   PALO = "
 				+ tp.getCard().getKind()
 				+ " El valor es "
 				+ tp.getCard().getValue());
 				System.out.println("El valor es " + tp.getValue());
 				
 				/*	try {
 				 
 							String tableid=String.valueOf(tableIdAux);
 							Table tabela = (Table)getTables().get(tableid);
 								TrucoGameClient trucoGame=(TrucoGameClient)tabela.getTGame();
 								trucoGame.play(te);
 				} catch (java.lang.NullPointerException e) {
 								System.out.println("LA TABLA ES NULL EN EL COMUNICATOR CLIENT Mtodo xmlReadCantarTanto ");
 								e.printStackTrace(System.out);
 										throw e;
 				}*/
 			}
 		}
 	}
 	private void cabecera(Document doc)
 	{
 		//saca la cabeza del paquete y envia el paquete al lector correspondiente
 		
 		List children = doc.getContent();
 		Iterator iterator = children.iterator();
 		Object child = iterator.next();
 		Element element = (Element) child;
 		String aux = element.getName();
 		//System.out.println(aux);
 		
 		if (aux.compareTo("PlayerSit") == 0)
 		{
 			//cuando ya se sento un Player
 			xmlReadPlayerSit(child);
 		}
 		if (aux.compareTo("ChatMsg") == 0)
 		{
 			System.out.println(
 			"Llego un mensaje de chat, soy " + player.getName());
 			xmlReadChatMsg(child);
 		}
 		
 		if (aux.compareTo("LoginOk") == 0)
 		{
 			System.out.println("LLego un mensaje de LoginOk");
 			xmlReadLoginOk(child);
 		}
 		if (aux.compareTo("UserJoined") == 0)
 		{
 			System.out.println("Llego un mensaje de UserJoined");
 			xmlReadUserJoined(child);
 		}
 		
 		if (aux.compareTo("Turno") == 0)
 		{
 			xmlReadTurno(child);
 		}
 		if (aux.compareTo("TerminalMessage") == 0)
 		{
 			//super.xmlReadTerminalMessage(child);
 		}
 		
 		if (aux.compareTo("TableCreated") == 0)
 		{
 			//cuando ya se creo una tabla
 			xmlReadTableCreated(child);
 		}
 		if (aux.compareTo("TableJoined") == 0)
 		{
 			//cuando ya se creo una tabla
 			xmlReadTableJoined(child);
 		}
 		if (aux.compareTo("TableLeft") == 0)
 		{
 			//TODO morgueada++: SE uso TableLeft para los nombres de los ELEMENT y TableKicked para los nombres de los metodos
 			//cuando ya se creo una tabla
 			xmlReadTableKicked(child);
 		}
 		if (aux.compareTo("GameStarted") == 0)
 		{
 			//cuando empezo el juego
 			xmlReadGameStarted(child);
 		}
 		if (aux.compareTo("SendCards") == 0)
 		{
 			//cuando se envia tres cartas
 			xmlreadSendCards(child);
 		}
 		if (aux.compareTo("CantarTanto") == 0)
 		{
 			//cuando se canta el tanto
 			xmlReadCantarTanto(child);
 		}
 		if (aux.compareTo("Canto") == 0)
 		{
 			//cuando se canta
 			xmlReadCanto(child);
 		}
 		if (aux.compareTo("Cardsend") == 0)
 		{
 			//cuando se envia una carta
 			xmlReadCard(child);
 		}
 		if (aux.compareTo("TrucoPlay") == 0)
 		{
 			xmlReadTrucoPlay(child);
 		}
 		if (aux.compareTo("InfoGame")== 0 )
 		{
 			xmlReadInfoGame(child);
 		}
 	}
 	
 	
 	/**
 	 * @param child
 	 */
 	String atabelaDaKicked;
 	String oTrucoPlayerDaTv;
 	private void xmlReadTableKicked(Object o) {
 		String aux;
 		System.out.println(
 		"Estamos dentro de xmlReadTableKicked-------------------------------");
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Player") == 0)
 			{
 				oTrucoPlayerDaTv = element.getAttributeValue("name");
 			}
 			if (aux.equals("Table"))
 			{
 				atabelaDaKicked = element.getAttributeValue("id");
 			}
 			
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTableKicked(child);
 			}
 			
 			//Por la morgueada de mezcla de nombres entre kicked y left
 			if (aux.equals("TableLeft"))
 			{
 				// Hacer lo que hay que hacer
 				
 				//Table t = (Table) getTables().get(atabelaDaKicked);
 				Table t=(Table)(pieza.getHashTable().get(new Integer(atabelaDaKicked)));
 				TrucoPlayer tp = (TrucoPlayer) pieza.getPlayer(oTrucoPlayerDaTv);
 				t.removePlayer(tp);
 				
 			}
 		}		
 	}
 
 	// TODO ESTE NO SE LLAMA NUNCA NI SIRVE PARA UN CARAJO APARENTE
 	// LO DEJAMOS PORSI
 	TrucoPlayer elPlayerDeAca2;
 	TrucoGameClient elGameDeAca;
 	
 	public void xmlReadTurno(Object o)
 	{
 		// Chau excepcin Feltstica
 		//new Exception("Nada implementado aun :-(     ").printStackTrace(System.out);
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				type = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Game") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				gameID = Integer.parseInt(element.getAttributeValue("id"));
 				//elGameDeAca = (TrucoGameClient)((Table)getTables().get(String.valueOf(gameID))).getTGame();
 				//cambiado por cricco
 				elGameDeAca = (TrucoGameClient)((Table)pieza.getHashTable().get(new Integer(gameID))).getTGame();
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				hand = Integer.parseInt(element.getAttributeValue("number"));
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				user = element.getAttributeValue("name");
 				elPlayerDeAca2 = pieza.getPlayer(user);
 			}
 			
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTurno(child);
 			}
 			if (aux.compareTo("Turno") == 0)
 			{
 				System.out.println("Leyento paquete turno");
 				// TODO Quiloooooooombo armamos ac con el gameNumber y tableNumber
 				TrucoEvent te =
 				new TrucoEvent(
 				elGameDeAca,
 				hand,
 				elPlayerDeAca2,
 				(byte) type);
 				elGameDeAca.turn(te);
 				System.out.println("Tipo:" + type);
 				System.out.println("Game:" + gameID);
 				System.out.println("Hand:" + hand);
 				System.out.println("Player:" + elPlayerDeAca2.getName());
 				
 			}
 		}
 	}
 	
 	
 	TrucoCard cardAux;
 	TrucoPlayer elTpAsdf;
 	Table laTabelaAsdf;
 	public void xmlReadCard(Object  o)
 	{
 		System.out.println("Se recibe juego de carta :D jojo!");
 		
 		String aux;
 		System.out.println("----------------------\naca vamos a ver si pasa el instance of");
 		if (o instanceof Element)
 		{
 			System.out.println("aca si pasoo el instance of");
 			Element element = (Element)o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				typeAux = Integer.parseInt(element.getAttributeValue("id"));
 				System.out.println("en type");
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 				//laTabelaAsdf = (Table)(getTables().get(String.valueOf(tableIdAux)));
 				laTabelaAsdf = (Table)(pieza.getHashTable().get(new Integer(tableIdAux)));
 				System.out.println("en table");
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				handAux = Integer.parseInt(element.getAttributeValue("number"));
 				System.out.println("en hand");
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 				elTpAsdf = pieza.getPlayer(userAux);
 				System.out.println("Ya obtuve el Player"+userAux);
 			}
 			if (aux.compareTo("Carta") == 0)
 			{
 				String kind = element.getAttributeValue("kind");
 				String value = element.getAttributeValue("value");
 				kindCardAux = Byte.parseByte(kind);
 				valueCardAux = Byte.parseByte(value);
 				System.out.println("la carta ya se dese");
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadCard(child);
 			}
 			if (aux.compareTo("Cardsend") == 0)
 			{
 				/*TrucoEvent te = new TrucoEvent(
 				tableIdAux,
 				handAux,
 				elTpAsdf,
 				(byte) type,
 				cardAux);
 				 
 				System.out.println("Tabla :" + te.getTableNumber());
 				System.out.println("HAND : " + te.getNumberOfHand());
 				System.out.println(
 				"Truco Player : " + te.getTrucoPlayer().getName());
 				System.out.println("TYPE  : " + te.getTypeEvent());
 				System.out.println(
 				"LA CARTA   PALO = "
 				+ te.getCard().getKind()
 				+ " El valor es "
 				+ te.getCard().getValue());*/
 				TrucoPlayer myPlayer = pieza.getPlayer(userAux);
 				//TrucoGameClient myCliente = (TrucoGameClient)((Table)getTables().get(String.valueOf(gameID))).getTGame();
 				TrucoGameClient myCliente = (TrucoGameClient)((Table)pieza.getHashTable().get(new Integer(gameID))).getTGame();
 				TrucoCard myCard = myCliente.getCard(kindCardAux,valueCardAux);
 				System.out.println("1)Quiere Jugar + "+userAux);
 				System.out.println("2)Quiere Jugar + "+myPlayer.getName());
 				System.out.println("1) con carta+"+kindCardAux+","+valueCardAux);
 				if (myCard == null)
 				{
 					System.out.println("Carta es null");
 				}
 				System.out.println("2) con carta+"+myCard.getKind()+","+myCard.getValue());
 				
 				//myCliente.play(new TrucoPlay(myPlayer,TrucoPlay.JUGAR_CARTA,myCard));
 				
 				TrucoPlay tp = new TrucoPlay(myPlayer,TrucoPlay.JUGAR_CARTA,myCard);
 				if (getAssociatedPlayer() != myPlayer)
 				{
 					System.out.println("Ejecuto primero la jugada en el cliente!!!: " + tp.getPlayer().getName());
 					myCliente.play(tp);
 				}
 				
 				myCliente.playResponse(myPlayer,myCard);
 				System.out.println("Se jogou la carta?");
 				
 			}
 		}
 		
 	}
 	
 	
 	public void xmlReadCanto(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				typeAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				handAux = Integer.parseInt(element.getAttributeValue("number"));
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 				elPlayerDeAca3 = pieza.getPlayer(userAux);
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadCanto(child);
 			}
 			if (aux.compareTo("Canto") == 0)
 			{
 /*                TrucoEvent te = null;
 				//TODO A LO CHANCHO HASTA ARREGLAR CC
 				System.out.println("Tabla :" + te.getTableNumber());
 				System.out.println("HAND : " + te.getNumberOfHand());
 				System.out.println(
 				"Truco Player : " + te.getTrucoPlayer().getName());
 				System.out.println("TYPE  : " + te.getTypeEvent());
 				// TODO ACA DESCOMENTAMOS CODIGO PARA PROBAR*/
 				try
 				{
 					System.out.println("queriendo hacer jugada de canto!!!!");
 					//TrucoGameClient myCliente = (TrucoGameClient)((Table)getTables().get(String.valueOf(gameID))).getTGame();
 					TrucoGameClient myCliente = (TrucoGameClient)((Table)pieza.getHashTable().get(new Integer(gameID))).getTGame();
 					TrucoPlayer myPlayer = pieza.getPlayer(userAux);
 					TrucoEvent ev = new TrucoEvent(myCliente ,handAux,myPlayer,(byte)typeAux);
 					TrucoPlay tp = ev.toTrucoPlay();
 					if (getAssociatedPlayer() != myPlayer)
 					{
 						System.out.println("Ejecuto primero la jugada en el cliente!!!: " + tp.getPlayer().getName()+"jugada"+ev.getTypeEvent());
 						System.out.println("Ejecuto primero la jugada en el cliente!!!: " + tp.getPlayer().getName()+"jugada"+tp.getType());
 						myCliente.play(tp);
 					}
 					myCliente.playResponse(myPlayer,(byte)typeAux);
 					System.out.println("se cantou, furou?");
 					
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println("LA TABLA ES NULL EN EL COMUNICATOR CLIENT Mtodo xmlReadCantarTanto ");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 			}
 		}
 	}
 	TrucoPlayer elPlayerDeAca3;
 	public void xmlReadInfoGame(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Type") == 0)
 			{
 				typeAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				handAux = Integer.parseInt(element.getAttributeValue("number"));
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 				elPlayerDeAca3 = pieza.getPlayer(userAux);
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadInfoGame(child);
 			}
 			if (aux.compareTo("InfoGame") == 0)
 			{
 				TrucoEvent te = null;
 				//TODO A LO CHANCHO HASTA ARREGLAR CC
 				if (typeAux != 50)
 				{
 					te =
 					new TrucoEvent(
 					tableIdAux,
 					handAux,
 					mi_jugador,
 					(byte) typeAux);
 				} else
 				{
 					te =
 					new TrucoEvent(
 					tableIdAux,
 					handAux,
 					elPlayerDeAca3,
 					(byte) typeAux);
 				}
 				System.out.println("Tabla :" + te.getTableNumber());
 				System.out.println("HAND : " + te.getNumberOfHand());
 				System.out.println(
 				"Truco Player : " + te.getTrucoPlayer().getName());
 				System.out.println("TYPE  : " + te.getTypeEvent());
 				// TODO ACA DESCOMENTAMOS CODIGO PARA PROBAR
 				try
 				{
 					String tableid=String.valueOf(tableIdAux);
 					//Table tabela = (Table)getTables().get(tableid);
 					//la linea de arriba se cambia por la de abajo by Cricco
 					Table tabela=(Table)(pieza.getHashTable().get(new Integer(tableid)));
 					TrucoGameClient trucoGame=(TrucoGameClient)tabela.getTGame();
 					//if (te.getTypeEvent() != 50) {
 					if (userAux!=null)
 						te.setPlayer(pieza.getPlayer(userAux));
 					
 					trucoGame.play(te);
 					//} else {
 					//trucoGame.turn(te);
 					
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println("LA TABLA ES NULL EN EL COMUNICATOR CLIENT Mtodo xmlReadCantarTanto ");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 			}
 		}
 	}
 	
 	int tableIdAux;
 	int handAux;
 	TrucoCard[] cartas = new TrucoCard[3];
 	int currentCard = 0;
 	String userAux;
 	public void xmlreadSendCards(Object o)
 	{
 		tableIdAux = 0;
 		handAux = 0;
 		currentCard = 0;
 		xmlreadSendCardsAlg(o);
 		
 	}
 	
 	TrucoPlayer elPlayerDeAca = null;
 	public void xmlreadSendCardsAlg(Object o)
 	{
 		System.out.println("RECIBI YO UN PAQUETE DE CARTAS!!");
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("TrucoPlayer") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 				elPlayerDeAca = pieza.getPlayer(userAux);
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableIdAux = Integer.parseInt(element.getAttributeValue("id"));
 			}
 			if (aux.compareTo("Hand") == 0)
 			{
 				handAux = Integer.parseInt(element.getAttributeValue("number"));
 			}
 			if (aux.compareTo("Carta") == 0)
 			{
 				String kind = new String();
 				String value = new String();
 				kind = element.getAttributeValue("kind");
 				value = element.getAttributeValue("value");
 				cartas[currentCard] =
 				new TrucoCard(
 				Integer.parseInt(kind),
 				Integer.parseInt(value));
 				currentCard++;
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlreadSendCardsAlg(child);
 			}
 			if (aux.compareTo("SendCards") == 0)
 			{
 				//Chatpanel.showChatMessage(user,message);
 				TrucoEvent te = new TrucoEvent(tableIdAux,
 				handAux,
 				mi_jugador,
 				TrucoEvent.ENVIAR_CARTAS,
 				cartas);
 				System.out.println("tableID: "
 				+ (te.getTableNumber())
 				+ "\nHand"
 				+ te.getNumberOfHand()
 				+ "\nPlayer :"
 				+ (te.getPlayer()).getName());
 				TrucoCard[] cartasIMP = new TrucoCard[3];
 				System.out.println("*******Cartas*********");
 				cartasIMP = te.getCards();
 				for (int i = 0; i < 3; i++)
 				{
 					System.out.println(
 					"Palo:"
 					+ cartasIMP[i].getKind()
 					+ "Value: "
 					+ cartasIMP[i].getValue());
 					
 				}
 				
 				try
 				{
 					
 					System.out.println("Se recibe cartas\n");
 					String tableid=String.valueOf(tableIdAux);
 					//Table tabela = (Table)getTables().get(tableid);
 					//la linea de arriba se camibia por la de abajo
 					Table tabela=(Table)(pieza.getHashTable().get(new Integer(tableid)));
 					TrucoGameClient trucoGame=(TrucoGameClient)tabela.getTGame();
 					//ucoGame.play(te);
 					trucoGame.recibirCartas(elPlayerDeAca, cartasIMP);
 					System.out.println("Se recibio bien las cartas\n");
 					
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println("LA TABLA ES NULL EN EL COMUNICATOR CLIENT Mtodo xmlreadSendCardsAlg ");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 				finally
 				{
 					System.out.println("se recibio cartas :?");
 				}
 			}
 		}
 	}
 	
 	public void playResponse(TrucoEvent event)
 	{
 		//        TrucoPlay tp = event.toTrucoPlay();
 		//        if (tp.getPlayer() == mi_jugador){
 		//            Document doc = tp.toXml();
 		//            super.sendXmlPackage(doc);
 		/*Document doc= a.toXml();
 		 * super.sendXmlPackage(doc);
 		 *
 		 */
 		System.out.println("Void playResponse method in " + this.getClass().getName());
 		//        }
 		//        else{
 		//            System.out.println("************************\nCliente no quiere enviar jugada que no es de el ;)");
 		//        }
 	}
 	
 	public void play(TrucoEvent event)
 	{
 		//TODO. ver por que aca se usa TrucoEvent.jugar_carta y no truco play.jugar_carta !!!!!
 				/*
 				 * En el java console se sigue viendo q reparte por separado para cada table
 				 */
 /*        System.out.println("El pacochi debe viajar por los cables agora!");
 		System.out.println(
 		"player: " + event.getPlayer() + ", carta: " + event.getCard());
 		TrucoPlay tp;
 		//No se porque usan el campo "hand" para guardar el tipo de juego :-?
  
 		// Ac, en vez de getTypeEvent deca getHandNumber
 		if (event.getTypeEvent() == TrucoEvent.ENVIAR_CARTAS)
 			tp = new TrucoPlay(event.getPlayer(), TrucoEvent.ENVIAR_CARTAS);
 		else if (event.getTypeEvent() == TrucoEvent.JUGAR_CARTA)
 			tp =
 			new TrucoPlay(
 			event.getPlayer(),
 			TrucoPlay.JUGAR_CARTA,
 			event.getCard());
 		else {
 			new Exception(
 			//TODO PENSAR EN UN MTODO ESTTICO QUE TE DEVUELVA UN TP DADO UN TE O SI NO EN UNIFICAR LOS NROS.
 			"Aca posiblemente debe ir un cacho de _switch_ para cada jugada")
 			.printStackTrace(
 			System.out);
 			tp = null;
 		}
 		if (tp != null) {
 			Document doc = tp.toXml();
 			super.sendXmlPackage(doc);
 		} else
 			System.out.println(
 			"No hay beleza para el TrucoPlay en :" + getClass().getName());
  */
 		if (event.getPlayer() == getAssociatedPlayer())
 		{
 			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
 			System.out.println("El player del evento: " + event.getPlayer().getName());
 			System.out.println("El player asociado: " + getAssociatedPlayer().getName());
 			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
 			super.sendXmlPackage
 			(
 				event.toTrucoPlay().toXml()
 			);
 			System.out.println("DEntro de Play se va a enviar el play\n");
 			impXml(event.toTrucoPlay().toXml());
 		}
 		
 	}
 	public void impXml(Document doc)
 	{
 		try {
        
 				XMLOutputter serializer = new XMLOutputter("  ", true);
        
 				serializer.output(doc, System.out);
 			}
 			catch (IOException e) {
 			System.err.println(e);
 			}
 	}
 	
 	public void turn(TrucoEvent event)
 	{
 		//		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 		System.out.println(getClass().getName()+".turn() NO IMPLEMENTADO");
 		
 	}
 	public void endOfHand(TrucoEvent event)
 	{
 		//		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		System.out.println("Soy: " + getClass().getName() + ", voy a enviar el fin de mano");
 		Document doc = event.toTrucoPlay().toXml();
 		super.sendXmlPackage(doc);
 	}
 	public void cardsDeal(TrucoEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	public void handStarted(TrucoEvent event)
 	{
 		//Aca llega cuando el TrucoGame avisa que empezo la mano
 		//pero la primera vez viene de un evento que se inicio en el mismo CommunicatorClient
 		//posiblemente ya no hace falta volver a disparar este evento, puesto que eso se hace
 		//del lado del serva ahora
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	public void gameStarted(TrucoEvent event)
 	{
 		//Aca llega cuando Table avisa que se inicio correctamente el juego 10.08.2003
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	public void endOfGame(TrucoEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	
 	public void play(TrucoPlay tp)
 	{
 		
 		Document doc = tp.toXml();
 		super.sendXmlPackage(doc);
 		new Exception("Esto no deberia de funcionar!!!!!!!!!!!!!!!!! :-(   ").printStackTrace(System.out);
 	}
 	public TrucoPlayer getAssociatedPlayer()
 	{
 		return mi_jugador;
 	}
 	
 	int posAux;
 	//solo sirve para este metodo el de abajo xmlReadPlayerSitRequest
 	int idAux; //solo sirve para el metodo de abajo xmlReadPlayerSitRequest
 	String userAux2;
 	public void xmlReadPlayerSit(Object o)
 	{
 		//en verdad jugador no se usa porque creemos que como el communicator ya tiene su player entonces con
 		//ese player es suficiente
 		String aux;
 		
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Pos") == 0)
 			{
 				posAux = Integer.parseInt(element.getAttributeValue("pos"));
 				
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				userAux2 = element.getAttributeValue("name");
 				
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				
 				idAux = Integer.parseInt(element.getAttributeValue("id"));
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadPlayerSit(child);
 			}
 			if (aux.compareTo("PlayerSit") == 0)
 			{
 				System.out.println(
 				"Dentro del Communicator Client xmlReadPlayerSit");
 				System.out.println("El table a sentarse es" + idAux);
 				System.out.println("La posicion es" + posAux);
 				try
 				{
 					String tableid = String.valueOf(idAux);
 					//Table tabela = (Table) getTables().get(tableid);
 					//la linea de arriba se reemplaza por la de abajo by Cricco
 					Table tabela =(Table)(pieza.getHashTable().get(new Integer(tableid)));
 					System.out.println("Imprimir los tables");
 					py.edu.uca.fcyt.util.HashUtils.imprimirHash(pieza.getHashTable());
 					System.out.println("La tabla en la que se va a sentar es: "+tabela);
 					tabela.sitPlayer(pieza.getPlayer(userAux2), posAux);
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println(
 					"LA TABLA ES NULL EN EL COMUNICATOR CLIENT METODO xmlReadPlayerSit ");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 			}
 			
 		}
 	}
 	
 	public void xmlReadUserJoined(Object o)
 	{
 		String aux;
 		System.out.println(
 		"Estamos dentro de xmlReadUserJoines-------------------------------");
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Player") == 0)
 			{
 				System.out.println(
 				"Estamos dentro de xmlReadUserJoines//////  dentro de un player=======================================");
 				String jugname = element.getAttributeValue("name");
 				int rating =
 				Integer.parseInt(element.getAttributeValue("rating"));
 				if (mi_jugador == null)
 				{
 					mi_jugador = new TrucoPlayer(jugname, rating);
 					pieza.addPlayer(mi_jugador);
 					System.out.println(
 					"Jugador nuevo name="
 					+ mi_jugador.getName()
 					+ "rating"
 					+ mi_jugador.getRating());
 					pieza.loginCompleted(mi_jugador);
 					setPlayer(mi_jugador);
 				} else
 				{
 					pieza.addPlayer(new TrucoPlayer(jugname, rating));
 				}
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadUserJoined(child);
 			}
 		}
 	}
 	
 	String playerAux;
 	String tableidAux;
 	String playerAux4;
 	String tableidAux4;
 	public void xmlReadTableJoined(Object o)
 	{
 		String aux;
 		System.out.println(
 		"Estamos dentro de xmlReadTableJoined-------------------------------");
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Player") == 0)
 			{
 				System.out.println(
 				"Estamos dentro de xmlReadTableJoined//////  dentro de un player=======================================");
 				playerAux4 = element.getAttributeValue("name");
 			}
 			if (aux.equals("Table"))
 			{
 				tableidAux4 = element.getAttributeValue("id");
 			}
 			
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTableJoined(child);
 			}
 			
 			if (aux.equals("TableJoined"))
 			{
 				// Hacer lo que hay que hacer
 				//Table t = (Table) getTables().get(tableidAux4);
 				//La linea de arriba cambie por la de abajo by Cricco
 				Table t= (Table)(pieza.getHashTable().get(new Integer(tableidAux4)));
 				TrucoPlayer tp = (TrucoPlayer) pieza.getPlayer(playerAux4);
 				System.out.println(
 				"Voy a hacer un t.addPlayer de " + tp.getName());
 				System.out.println("Vamos a imprimr la tabla \n"+t);
 				t.addPlayer(tp);
 				
 				if (getAssociatedPlayer().getName().equals(tp.getName()))
 				{
 					t.show();
 				}
 			}
 		}
 	}
 	
 	public void xmlReadTableCreated(Object o)
 	{
 		String aux;
 		
 		if (o instanceof Element)
 		{
 			System.out.println("Detro de Table Created elementos: " + ((Element) o).getName());
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.equals("Player"))
 			{
 				playerAux = element.getAttributeValue("name");
 			}
 			if (aux.equals("Table"))
 			{
 				
 				tableidAux = element.getAttributeValue("id");
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTableCreated(child);
 			}
 			if (aux.equals("TableCreated"))
 			{
 				
 				Table t = null;
 				TrucoPlayer tp = pieza.getPlayer(playerAux);
 				
 				if (getAssociatedPlayer().getName().equals(tp.getName()))
 				{
 					t = new Table(getAssociatedPlayer(), true);
 					t.setTableNumber(Integer.parseInt(tableidAux));
 					t.addTableListener(this);
 					getTables().put(tableidAux, t);
 					System.out.println("En el cliente, voy a crear un table.!");
 					this.pieza.addTable(t);
 					
 					t.addPlayer(tp);
 					
 					t.show();
 				} else
 				{
 					t = new Table(getAssociatedPlayer(), false);
 					t.setTableNumber(Integer.parseInt(tableidAux));
 					t.addTableListener(this);
 					getTables().put(tableidAux, t);
 					System.out.println("En el cliente, voy a crear un table.!");
 					this.pieza.addTable(t);
 					t.addPlayer(tp);
 				}
 				
 			}
 		}
 	}
 	
 	public void xmlReadLoginOk(Object o)
 	{
 		Players.removeAllElements();
 		Mesas.removeAllElements();
 		current = 0;
 		xmlReadLoginAlg(o);
 	}
 	
 	private void xmlReadLoginAlg(Object o)
 	{
 		//
 		String aux;
 		
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			System.out.println("Estamos dentro del xmlReadLoginAlg");
 			if (aux.compareTo("Player") == 0)
 			{
 				String jugname = element.getAttributeValue("name");
 				int rating =
 				Integer.parseInt(element.getAttributeValue("rating"));
 				TrucoPlayer jug1 = new TrucoPlayer(jugname, rating);
 				Players.add(jug1);
 				System.out.println("Player: " + jugname + "Rating: "+rating);
 				
 				if (!jug1.getName().equals(getAssociatedPlayer().getName()))
 					pieza.addPlayer(jug1);
 			}
 			if (aux.compareTo("Playert") == 0)
 			{
 				String jugname = element.getAttributeValue("name");
 			
 				int chair=Integer.parseInt(element.getAttributeValue("chair"));
 				System.out.println("Player table: "+jugname +" en la silla: "+chair);
 				TrucoPlayer jug=pieza.getPlayer(jugname);
 				//aca en vez de instanciar hay que obtener la ref
 				//verdadera al jugador juganame
 				//en table hay que hacer el addtable que ande verdaderamente
 				Table tabela=(Table)(Mesas.get(current));
 				
 				tabela.addPlayer(jug);
				
				tabela.sitPlayer(jug,chair);
				
 				//mesa.addPlayer(jugaux);
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				String host=element.getAttributeValue("host");
 				
 				TrucoPlayer jughost=pieza.getPlayer(host);
 				//Table mesa=new Table(jughost,true);
 				String tableid=element.getAttributeValue("number");
 				//mesa.setTableNumber(number);
 				current=Mesas.size();
 				
 				System.out.println("La mesa: "+tableid +"Con el host: "+ host);
 				
 								
 				Table t = null;
 				if (getAssociatedPlayer().getName().equals(jughost.getName()))
 				{
 					t = new Table(getAssociatedPlayer(), true);
 					t.setTableNumber(Integer.parseInt(tableid));
 					t.addTableListener(this);
 					getTables().put(new Integer(tableid), t);
 					System.out.println("En el cliente, voy a crear un table.!");
 					this.pieza.addTable(t);
 					//t.addPlayer(jughost);
 					Mesas.add(t);
 					//t.show();
 				} else
 				{
 					t = new Table(getAssociatedPlayer(), false);
 					t.setTableNumber(Integer.parseInt(tableid));
 					t.addTableListener(this);
 					getTables().put(new Integer(tableid), t);
 					System.out.println("En el cliente, voy a crear un table.!");
 					this.pieza.addTable(t);
 					//t.addPlayer(jughost);
 					Mesas.add(t);
 				}
 				
 				
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadLoginAlg(child);
 			}
 			if (aux.compareTo("LoginOk") == 0)
 			{
 				
 				TrucoPlayer jug;
 				Table mesa1;
 	
 			
 				/*for (Enumeration e = Mesas.elements(); e.hasMoreElements();)
 				{
 					mesa1 = (Table) e.nextElement();
 					
 					System.out.println(
 					"==================  RECIBI Table number: "
 					+ mesa1.getTableNumber()
 					+ "\n");
 					Vector jugadores = (Vector) mesa1.getPlayers();
 										for(Enumeration e2=jugadores.elements();e2.hasMoreElements();) {
 										jug=(TrucoPlayer)e2.nextElement();
 										System.out.println("en table======================RECIBI Player: "+jug.getName()+" Rating: "+jug.getRating()+"\n");
 										}
 					
 					pieza.addTable(mesa1);// Info de la pieza comentado para pruebas de Redes descomentar para que ande
 				
 					
 				
 				}*/
 			} 	
 		}
 		
 	}
 	
 	/** <p>
 	 * Does ...
 	 * </p><p>
 	 *
 	 * @param chatPanelContainer ...
 	 * </p><p>
 	 * @param player ...
 	 * </p><p>
 	 * @param htmlMessage ...
 	 * </p><p>
 	 *
 	 * </p>
 	 *
 	 */
 	
 	/** <p>
 	 * Does ...
 	 * </p><p>
 	 *
 	 * </p>
 	 *
 	 */
 	public void gameStartRequested()
 	{
 		
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	
 	public void gameStartRequest(TableEvent te)
 	{
 		System.out.println("Imprim ya algo en gameStartRequest del CC");
 		Document doc = te.toXml();
 		super.sendXmlPackage(doc);
 		
 	}
 	String equipoAux1; //
 	String equipoAux2; //
 	public void xmlReadGameStarted(Object o)
 	{
 		// PAULO
 		String aux;
 		
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Equipo1") == 0)
 			{
 				equipoAux1 = element.getAttributeValue("equipo1");
 			}
 			
 			if (aux.compareTo("Equipo2") == 0)
 			{
 				equipoAux2 = element.getAttributeValue("equipo2");
 			}
 			if (aux.compareTo("Table") == 0)
 			{
 				idAux = Integer.parseInt(element.getAttributeValue("id"));
 				System.out.println(
 				"El id de la tabla es:" + element.getAttributeValue("id"));
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadGameStarted(child);
 			}
 			if (aux.compareTo("GameStarted") == 0)
 			{
 				System.out.println("Equipo1 :" + equipoAux1);
 				System.out.println("Equipo2: " + equipoAux2);
 				System.out.println("Table : " + idAux);
 				//ESTO ESTA COMENTADO PORQUE hay metodos no implementados todavia en el TableServer
 								/* try {
 									Table tabela = (Table)getTables().get(idAux);
 								 
 								Team team1=new Team(equipoAux1);
 								Team team2=new Team(equipoAux1);
 								TrucoGame truco=new TrucoGame(team1,team2);
 								tabela.startGame(truco);
 								} catch (java.lang.NullPointerException e) {
 								System.e.println("LA TABLA ES NULL EN EL COMUNICATOR CLIENT EN EL Mtodo xmlReadGameStarted");
 								e.printStackTrace(System.out);
 								throw e;
 								}*/
 				TrucoGameClient tGame;
 				TrucoTeam tTeams[];
 				
 				System.out.println("Requesting client game start...");
 				
 				//Table t = (Table) (getTables().get(String.valueOf(idAux)));
 				//La linea de arriba fue cambiada por Cricco vale abajo
 				Table t=(Table)(pieza.getHashTable().get(new Integer(idAux)));
 				
 				tTeams = t.createTeams();
 				
 				// se crea el TrucoGame con los teams creados
 				
 				equipo1=tTeams[0];
 				equipo2=tTeams[1];
 				tGame = new TrucoGameClient(tTeams[0], tTeams[1]);
 				
 				// se llama al 'startGame' de todas las tablas
 				tGame.addTrucoListener(this);
 				t.startGame(tGame);
 				// da la orden de inicio de juego a 'tGame'
 				tGame.startGameClient();
 				System.out.println("Parece que el juego empezo hei Communicator Client");
 				
 			}
 		}
 		
 	}
 	public void chatMessageSent(
 	ChatPanelContainer cpc,
 	TrucoPlayer player,
 	String htmlMessage)
 	{
 		
 	}
 	
 	/** Getter for property player.
 	 * @return Value of property player.
 	 *
 	 */
 	public TrucoPlayer getPlayer()
 	{
 		return this.mi_jugador;
 	}
 	
 	/** Setter for property player.
 	 * @param player New value of property player.
 	 *
 	 */
 	public void setPlayer(TrucoPlayer player)
 	{
 		this.player = player;
 	}
 	
 	/** <p>
 	 * Does ...
 	 * </p><p>
 	 *
 	 * @param ev ...
 	 * </p><p>
 	 *
 	 * </p>
 	 *
 	 */
 	public void createTableRequested(RoomEvent ev)
 	{
 		//peticion de createTable
 		System.out.println("Estoy en el CC: createTableRequested");
 		Document doc;
 		doc = super.xmlCreateTableRequested(ev);
 		super.sendXmlPackage(doc);
 	}
 	public Document xmlCreateTableJoinRequested(RoomEvent te)
 	{
 		Element ROOT = new Element("TableJoinRequest");
 		Element PLAYER = new Element("Player");
 		Element TABLE = new Element("Table");
 		//mi_jugador=new TrucoPlayer("Dani",10);
 		
 		PLAYER.setAttribute("name", te.getPlayer().getName());
 		
 		TABLE.setAttribute("id", String.valueOf(te.getTableNumber()));
 		ROOT.addContent(PLAYER);
 		ROOT.addContent(TABLE);
 		
 		Document doc = new Document(ROOT);
 		return doc;
 	}
 	public void tableJoinRequested(RoomEvent ev)
 	{
 		Document doc = xmlCreateTableJoinRequested(ev);
 		super.sendXmlPackage(doc);
 	}
 	public void gameFinished(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void gameStarted(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 	}
 	
 	public void playerKickRequest(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void playerKicked(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void playerSit(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void playerSitRequest(TableEvent event)
 	{
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	
 	public void playerStandRequest(TableEvent event)
 	{
 	}
 	
 	public void playerStanded(TableEvent event)
 	{
 	}
 	
 	public void showPlayed(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void signSendRequest(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 	public void playerJoined(TrucoPlayer player)
 	{
 		//new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		System.out.println("no implementado -> " + getClass().getName()+ ".playerJoined(TrucoPlayer)");
 		
 	}
 	
 	public void signSent(TableEvent event)
 	{
 		new Exception("Nada implementado aun :-(   ").printStackTrace(System.out);
 		
 	}
 	
 		/*  public static void main(String[] args)
 		{
 				RoomEvent te=new RoomEvent();
 								te.setTableNumber(10);
 		 
 				CommunicatorClient cc=new CommunicatorClient();
 								Document doc=cc.xmlCreateTableJoinRequested(te);
 				try {
 		 
 				XMLOutputter serializer = new XMLOutputter("  ", true);
 		 
 				serializer.output(doc, System.out);
 		 
 						}
 				catch (IOException e) {
 				System.out.println(e);
 				}
 				//        cc.
 		 (doc);
 		}*/
 	/*private TrucoPlayer getPlayer (String youName){
 		TrucoPlayer tp = equipo1.getPlayer(youName);
 		if (tp == null){
 			return equipo2.getPlayer(youName);
 		}
 		return tp;
 	}*/
 	
 }
