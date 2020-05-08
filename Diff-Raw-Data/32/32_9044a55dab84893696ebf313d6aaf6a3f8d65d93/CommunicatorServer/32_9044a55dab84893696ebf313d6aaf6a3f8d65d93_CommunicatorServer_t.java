 package py.edu.uca.fcyt.toluca.net;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.*;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 import py.edu.uca.fcyt.game.ChatPanelContainer;
 import py.edu.uca.fcyt.net.XmlPackagesSession;
 import py.edu.uca.fcyt.toluca.RoomServer;
 import py.edu.uca.fcyt.toluca.event.RoomEvent;
 import py.edu.uca.fcyt.toluca.event.TableEvent;
 import py.edu.uca.fcyt.toluca.event.TrucoEvent;
 import py.edu.uca.fcyt.toluca.game.TrucoCard;
 import py.edu.uca.fcyt.toluca.game.TrucoGame;
 import py.edu.uca.fcyt.toluca.game.TrucoPlay;
 import py.edu.uca.fcyt.toluca.game.TrucoPlayer;
 import py.edu.uca.fcyt.toluca.table.TableServer;
 import py.edu.uca.fcyt.toluca.game.TrucoTeam;
 import py.edu.uca.fcyt.toluca.table.Table;
 /**
  *
  * @author  PABLO JAVIER
  */
 public class CommunicatorServer extends Communicator
 {
 	TrucoTeam equipos[];
 	public void showPlayed(TableEvent te)
 	{
 		
 	}
 	public void signSent(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	public void playerSit(TableEvent te)
 	{
 		Document doc = te.toXml();
 		super.sendXmlPackage(doc);
 		
 	}
 	public void playerSitRequest(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	public void playerKicked(TableEvent te)
 	{
 		Document doc = te.toXml();
 		super.sendXmlPackage(doc);
 	}
 	public void playerKickRequest(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	public void playerStanded(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	public void playerStandRequest(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	public void gameFinished(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	
 	public void signSendRequest(TableEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	
 	public void gameStarted(TableEvent te)
 	{
 		
 		// Hay que hacer un getTeams()
 		// Instanciar el TrucoGame
 		// Setear al table server asociado el trucoGame correspondiente (el ID viene en el TableEvent te)
 		// Colocarse como listener del trucoGame
 		// Crear el XML con: te.xmlCreateGameStarted(String, String) que son los teams correspondientes
 		// Enviar el paquete al Cliente
 		
 		//te.xmlCreateGameStarted("Hola", "Hola");
 		
 		/*  esto pas al table server
 				TrucoGame tGame;
 				TrucoTeam tTeams[];
 				System.out.println("Requesting game start...");
 				tTeams = te.getTableServer().createTeams();
 				// se crea el TrucoGame con los teams creados
 				tGame = new TrucoGame(tTeams[0], tTeams[1]);
 				tGame.addTrucoListener(te.getTableServer());
 		 */
 		
 		TrucoGame tGame = te.getTableServer().getTrucoGame();
 		equipos=te.getTableServer().createTeams();
 		tGame.addTrucoListener(this);
 		
 		Document doc = te.xmlCreateGameStarted(equipos[0].getName(), equipos[1].getName());
 		
 		super.sendXmlPackage(doc);
 		
 		// se llama al 'startGame' de todas las tablas
 		
 		// da la orden de inicio de juego a 'tGame'
 		//tGame.startGame();
 		
 	}
 	/** Creates a new instance of ChatSessionServer */
 	/*public CommunicatorServer(RoomSer pieza) {
 		super(pieza);
 	}*/
 	
 	public CommunicatorServer()
 	{
 		super();
 	}
 	public String getInitErrorMessage(int errcode)
 	{
 		return "Sin errores";
 	}
 	
 	public int init()
 	{
 		return XmlPackagesSession.XML_PACKAGE_SESSION_INIT_OK;
 	}
 	private int i = 0;
 	
 	public void receiveXmlPackage(Element xmlPackage)
 	{
 		//		doc.addContent(xmlPackage);
 		try
 		{
 			
 			XMLOutputter serializer = new XMLOutputter("  ", true);
 			serializer.output(xmlPackage, System.out);
 			
 		} catch (IOException e)
 		{
 			System.out.println(e);
 		}
 		System.out.println("Vino un paquete: " + xmlPackage.getName());
 		Document doc = new Document();
 		if (xmlPackage.getParent() != null)
 		{
 			Element parent = xmlPackage.getParent();
 			System.out.println("El PADRE: " + parent.getName());
 		} else
 		{
 			System.out.println("El Padre es nuuuuuulo");
 		}
 		doc.setRootElement(xmlPackage);
 		System.out.println("//////////////////////////////////////////");
 		cabecera(doc);
 		/*
 						 TrucoPlayer p=new TrucoPlayer("Dani",10);
 		 
 		 
 				TrucoCard cars [] = new TrucoCard[3];
 						cars[0]=new TrucoCard(2,3);
 						cars[1]=new TrucoCard(2,4);
 						cars[2]=new TrucoCard(3,7);
 		 
 			   TrucoEvent prueba=new TrucoEvent(5,10,p,TrucoEvent.JUGAR_CARTA,cars[2]);
 						play(prueba);*/
 		//cardsDeal(prueba);
 		//loginComplete();
 		
 	}
 	
 	public void receiveXmlPackageWithParsingError(String rawXmlPackage)
 	{
 		System.out.println("Error en el parsing del XML");
 	}
 	
 	
 	ConnectionManager trucoServer;
 	
 	private RoomServer pieza;
 	
 	public void setTrucoServer(ConnectionManager trucoServer)
 	{
 		this.trucoServer = trucoServer;
 	}
 	public void loginFailed(RoomEvent te)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	/**
 	 *  Se dispara cuando el usuario de une a un room.
 	 *  Se guarda la referencia al TrucoPlayer
 	 */
 	public void playerJoined(TrucoPlayer player)
 	{
 		System.out.println("COMMSRV -> Player Joined DE: " + player.getName());
 		this.player = player;
 		Document doc = super.xmlCreateUserJoined(player);
 		super.sendXmlPackage(doc);
 	}
 	public void loginCompleted(RoomEvent re)
 	{
 		//RoomEvent te;
 		
 		/*
 				RoomEvent te=new RoomEvent();
 				Vector jugadores=new Vector();
 				Vector mesas=new Vector();
 				Player jug1=new Player("Daniel",10);
 				Player jug2=new Player("Jos",20);
 		 
 				Table mesa1=new Table();
 				Table mesa2=new Table();
 				jugadores.add(jug1);
 				jugadores.add(jug2);
 				mesa1.setTableNumber(2);
 		 
 				mesa1.addPlayer(jug1);
 				mesa1.addPlayer(jug2);
 				mesa2.setTableNumber(10);
 				mesa2.addPlayer(jug1);
 				mesa2.addPlayer(jug2);
 				mesas.add(mesa1);
 				mesas.add(mesa2);
 				te.setTabless(mesas);
 				te.setPlayerss(jugadores);
 		 
 				te.setUser("AAdaniAA");
 				te.setPassword("AAcriccoAAA");
 		 *
 		 **/
 		
 		Document doc;
 		//re.setPlayerss(pieza.getPlayerss());
 		System.out.println("Login Completed");
 		System.out.println("antes de cargar tables");
 		impVector((Vector)re.getTabless());
 		re.setPlayerss(pieza.getVPlayers());
 		re.setTabless(pieza.getVTables());
 		System.out.println("Despues de cargar tables");
 		impVector((Vector)re.getTabless());
 		
 		doc = super.xmlCreateLoginOk(re);
 		System.out.println("*************Paquete login que va al cliente ************");
 		impXml(doc);
 		super.sendXmlPackage(doc);
 	}
 	public void impVector(Vector myvec)
 	{
 		System.out.println("Dentro de imp Vector");
 		for(Enumeration e=myvec.elements();e.hasMoreElements();)
 		{
 			System.out.println("Dentro imp Vector bucle");
 			System.out.println(e.nextElement());
 			
 		}
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
 				//pieza.fireChatSent(user,message);
 				System.out.println("Player: " + user);
 				System.out.println("Mensaje: " + message);
 				System.out.println("Enviando Respuesta");
 				//                chatMessageSent(new Player(user,0),message);
 				// Aca vemos para quin puta es el mensaje
 				String origen = element.getAttributeValue("origin");
 				System.out.println("El origen es: " + origen);
 				if (origen.equalsIgnoreCase("room"))
 				{
 					pieza.sendChatMessage(new TrucoPlayer(user, 0), message);
 				} else
 				{
 					//TableServer t = (TableServer) getTables().get(origen);
 					TableServer t=(TableServer)(pieza.getHashTable().get(new Integer(origen)));
 					System.out.println("Dentro del CServer chat message la tabla es "+t);
 					
 					if (t != null)
 					{
 						//t.sendChatMessage(new TrucoPlayer(user, 0), message);
 						t.sendChatMessage(pieza.getPlayer(user),message);
 						
 					} else
 					{
 						System.out.println(
 						"NO PUDIMOS ENTREGAR EL CHAT!!! !@@!#$#@@Q");
 					}
 				}
 			}
 		}
 	}
 	TrucoCard cardAux; //variables chanchas
 	int typeAux;
 	int tableIdAux;
 	String userAux;
 	int valueAux;
 	/**
 	 *   En realidad parece que es un read truco event, no play.
 	 * @param o
 	 */
 	TrucoPlayer elPlayerDeAcaOtraVez;
 	public void xmlReadTrucoPlay(Object o)
 	{
 		//TableServer tabela =  (TableServer) getTables().get(String.valueOf(tableIdAux));
 		//Cambiado por Cricco vale la de abajo
 		TableServer tabela=(TableServer)(pieza.getHashTable().get(new Integer(tableIdAux)));
 		
 		TrucoGame tgame = tabela.getTrucoGame();
 		String aux;
 		if (o instanceof Element)
 		{
 			System.out.println("gueno hace instance of en server!");
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
 				elPlayerDeAcaOtraVez = pieza.getPlayer(userAux);
 			}
 			if (aux.compareTo("Carta") == 0)
 			{
 				String kind = element.getAttributeValue("kind");
 				String value = element.getAttributeValue("value");
 				cardAux = tgame.getCard(Byte.parseByte(kind),Byte.parseByte(value));
 				
 			}
 			if (aux.compareTo("Value") == 0)
 			{
 				
 				valueAux = Integer.parseInt(element.getAttributeValue("val"));
 				//if (valueAux == )
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
 				// chila
 				TrucoPlay tp = new TrucoPlay
 				(
 					tableIdAux,
 					elPlayerDeAcaOtraVez,
 					(byte) typeAux,
 					cardAux,
 					valueAux
 				);
 				
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
 				//ESTO HAY QUE DESCOMENTAR PARA QUE FUNCIONE ESTA COMENTADO PORQUE TODAVIA NO EXISTEN los metodos para el TableServer
 				try
 				{
 					tgame.play(tp);
 					System.out.println("Se hizo una jugada con exito :?");
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println("LA TABLA ES NULL EN EL COMUNICATOR SERVER Mtodo xmlReadTrucoPlay");
 					e.printStackTrace(System.out);
 					//					throw e;
 				}
 			}
 		}
 	}
 	
 	public Document xmlCreateTableJoined(RoomEvent re)
 	{
 		
 		Element ROOT = new Element("TableJoined");
 		Element PLAYER = new Element("Player");
 		PLAYER.setAttribute("name", re.getPlayer().getName());
 		Element TABLE = new Element("Table");
 		TABLE.setAttribute(
 		"id",
 		String.valueOf(re.getTableServer().getTableNumber()));
 		ROOT.addContent(PLAYER);
 		ROOT.addContent(TABLE);
 		Document doc = new Document(ROOT);
 		return doc;
 		
 	}
 	
 	public void tableJoined(RoomEvent ev)
 	{
 		Document doc;
 		
 		doc = xmlCreateTableJoined(ev);
 		super.sendXmlPackage(doc);
 		
 	}
 	
 	public void cabecera(Document doc)
 	{ //saca la cabeza del paquete y envia el paquete al lector correspondiente
 		
 		List children = doc.getContent();
 		Iterator iterator = children.iterator();
 		Object child = iterator.next();
 		Element element = (Element) child;
 		String aux = element.getName();
 		//System.out.println(aux);
 		
 		if (aux.compareTo("PlayerSitRequest") == 0)
 		{
 			xmlReadPlayerSitRequest(child);
 		}
 		
 		if (aux.compareTo("Login") == 0)
 		{
 			xmlReadLogin(child);
 		}
 		if (aux.compareTo("ChatMsg") == 0)
 		{
 			xmlReadChatMsg(child);
 		}
 		if (aux.compareTo("CreateTable") == 0)
 		{
 			xmlReadCreateTable(child);
 		}
 		if (aux.compareTo("TableJoinRequest") == 0)
 		{
 			System.out.println("Dentro de cabecera para ejecutar el metodo\n");
 			xmlReadTableJoinRequest(child);
 		}
 		if (aux.compareTo("GameStartRequest") == 0)
 		{
 			xmlReadGameStartRequest(child);
 		}
 		if (aux.compareTo("TrucoPlay")== 0){
 			xmlReadTrucoPlay(child);
 		}
 		if (aux.compareTo("TrucoGameInfo")== 0){
 			xmlReadTrucoGameInfo(child);
 		}
 		if (aux.compareTo("TableLeftRequest")== 0){
 			xmlReadTableLeftRequest(child);
 		}
 
 	}
 	/**
 	 * @param child
 	 */
 	private void xmlReadTableLeftRequest(Object o) {
 		String aux;
 		System.out.println("Leyendo el paquete");
 
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableid = element.getAttributeValue("id");
 				System.out.println("El table id es" + tableid);
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTableLeftRequest(child);
 			}
 			if (aux.compareTo("TableLeftRequest") == 0)
 			{
 				try
 				{
 					//TableServer tstmp = (TableServer) getTables().get(tableid);
 					TableServer tstmp=(TableServer)(pieza.getHashTable().get(new Integer(tableid)));
 					
 					TrucoPlayer tptmp = pieza.getPlayer(userAux);
 										
 					//TableEvent te = new TableEvent(TableEvent.EVENT_playerKickRequest, tstmp, tptmp, 0);					
 					tstmp.kickPlayer(tptmp);
 										
 				} catch (NullPointerException npe)
 				{
 					System.out.println("TABLA NUUUUUUUUUUUUUUULA :(( :((!!!!");
 					System.out.println(
 					"Player NUUUUUUUUUUUUULO :(( :(( ::((@!!!!!");
 					npe.printStackTrace(System.out);
 				}
 				
 			}
 		}
 	}
 	public void xmlReadTrucoGameInfo (Object o){
 		//TableServer tabela =  (TableServer) getTables().get(String.valueOf(tableIdAux));
 		TableServer tabela=(TableServer)(pieza.getHashTable().get(new Integer(tableIdAux)));
 		
 		TrucoGame tgame = tabela.getTrucoGame();
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
 				elPlayerDeAcaOtraVez = pieza.getPlayer(userAux);
 			}
 			if (aux.compareTo("Carta") == 0)
 			{
 				String kind = element.getAttributeValue("kind");
 				String value = element.getAttributeValue("value");
 				cardAux = tgame.getCard(Byte.parseByte(kind),Byte.parseByte(value));
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
 				xmlReadTrucoGameInfo(child);
 			}
 			if (aux.compareTo("TrucoGameInfo") == 0)
 			{
 				tgame.startHand(elPlayerDeAcaOtraVez);
 				//ESTO HAY QUE DESCOMENTAR PARA QUE FUNCIONE ESTA COMENTADO PORQUE TODAVIA NO EXISTEN los metodos para el TableServer
 				/*try
 				{
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println("LA TABLA ES NULL EN EL COMUNICATOR SERVER Mtodo xmlReadTrucoPlay");
 					e.printStackTrace(System.out);
 					//					throw e;
 						}
 					*/}				
 			}
 	}
 	public void xmlReadGameStartRequest(Object o)
 	{ // PAULO
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Table") == 0)
 			{
 				int tableid = Integer.parseInt(element.getAttributeValue("id"));
 				System.out.println(
 				"LLego un game Start Request al TABLE " + tableid);
 				//ESTO HAY QUE DESCOMENTAR PARA QUE FUNCIONE ESTA COMENTADO PORQUE TODAVIA NO EXISTEN los metodos para el TableServer
 				try
 				{
 					//TableServer tabela =
 					//(TableServer) getTables().get(String.valueOf(tableid));
 					TableServer tabela=(TableServer)(pieza.getHashTable().get(new Integer(tableid)));
 					
 					tabela.startGame();
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println(
 					"LA TABLA ES NULL EN EL COMUNICATOR SERVER METODO xmlReadGameStartRequest");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadGameStartRequest(child);
 			}
 			
 		}
 	}
 	int posAux;
 	//solo sirve para este metodo el de abajo xmlReadPlayerSitRequest
 	int idAux; //solo sirve para el metodo de abajo xmlReadPlayerSitRequest
 	String otroPlayerName = "";
 	public void xmlReadPlayerSitRequest(Object o)
 	{ //en verdad jugador no se usa porque creemos que como el communicator ya tiene su player entonces con
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
 				otroPlayerName = element.getAttributeValue("name");
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
 				xmlReadPlayerSitRequest(child);
 			}
 			if (aux.compareTo("PlayerSitRequest") == 0)
 			{
 				System.out.println(
 				"DENTRO DEL COMMUNICATOR SERVer carajo////////////////");
 				System.out.println("El table a sentarse es " + idAux);
 				System.out.println("La posicion es " + posAux);
 				//ATENCION ESTE TRY HAY QUE DESCOMENTAR PARA QUE FUNCIONE. ESTA COMOENTADO PORQUE EL TABLESERVER
 				//TODAVIA NO TIENE UN Mtodo PlayerSit
 				try
 				{
 					String tid = String.valueOf(idAux);
 					//TableServer tabela = (TableServer) getTables().get(tid);
 					//La linea de arrriba fue cambiada por cricco ahora se usa la de abajo
 					TableServer tabela=(TableServer)(pieza.getHashTable().get(new Integer(tid)));
 					System.out.println("********#######Dentro de sit 1\n"+tabela);
					if(tabela.getPlayerManager().getChair(pieza.getPlayer(otroPlayerName))>=0)
					{
						System.out.println("El player ya esta sentado jodetec citttt");
					} 
					else
					{
					
 					tabela.sitPlayer(pieza.getPlayer(otroPlayerName), posAux);
 					
					System.out.println("********#######Dentro de sit 2\n"+tabela);
					}
 				} catch (java.lang.NullPointerException e)
 				{
 					System.out.println(
 					"LA TABLA ES NULL EN EL COMUNICATOR SERVER METODO xmlReadPlayerSitRequest ");
 					e.printStackTrace(System.out);
 					throw e;
 				}
 				
 			}
 			
 		}
 	}
 	
 	/*    public void chatMessageSent(TrucoPlayer jug,String message) {
 		System.out.println("Enviando msg de chat del jug: " + jug.getName());
 		Document doc;
 		doc=super.xmlCreateChatMsg(jug,message);
 		super.sendXmlPackage(doc);
 		//        pieza.sendChatMessage(jug, message);
 	}
 	 */
 	
 	
 	
 	public void play(TrucoPlay te)
 	{
 		/*System.out.println("What the fuck @#!@$!!!! " + this.getClass().getName());
 		Document doc = te.toXml();
 		super.sendXmlPackage(doc);*/
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	
 	public void play(TrucoEvent event)
 	{/*preguntar bien por eso, estoy mareado. julio*/
 		//Cantartanto con value
 		//canto sin value
 		//card semd
 		//Document doc = event.toXml();
 		//super.sendXmlPackage(doc);
 		//System.out.println("Void play method in " + this.getClass().getName());
 		System.out.println("voy a enviar el pacote TrucoEvent en el m'etodo PLayRespone");
 		//Document doc = tp.toXml();
 		
 		//Con este nhembo q jugaba pero no iniciaba la 2da. ronda
 		//Sin este no funca ni siquiera la primera mano :-(
 		//Sospecho q es porque falta la implementacion para todos los casos del toTrucoPlay del TrucoEvent
 		Document doc = event.toXml();
 		
 		//Con esta linea se va al mazo. No juega nada :-(  Document doc = event.toTrucoPlay().toXml();
 		if(doc == null) //BIEN DRAGONICO, hasta q se convierta bien a TrucoPlay
 			//Da null cuando se termina una ronda, y tiene q empezar la segunda y los jugadores
 			//van preparandose a travez del boton OK
 			sendXmlPackage(event.toTrucoPlay().toXml());
 		else
 			super.sendXmlPackage(doc);
 	}
 	
 	public void playResponse(TrucoEvent event)
 	{
 		//TrucoPlay tp = event.toTrucoPlay();
 				/*if (tp.getPlayer() == getAssociatedPlayer()){/*no le devuelvo su jugada
 					System.out.println("Server no quiere devolver jugada de su jugador"+tp.getPlayer().getName());
 					System.out.println("que es "+getAssociatedPlayer().getName());
 				 
 					return;
 				}*/
 		new Throwable("Nada implementado aun :-( ").printStackTrace(System.out);
 	}
 	
 	
 	public void turn(TrucoEvent event)
 	{
 		/*Document doc = event.toXml();
 		super.sendXmlPackage(doc);*/
 		//new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 		System.out.println(getClass().getName()+".turn() NO IMPLEMENTADO");
 	}
 	public void endOfHand(TrucoEvent event)
 	{
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	public void cardsDeal(TrucoEvent event)
 	{
 		System.out.println("Vienen las CARTAS carajo"+this);
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	public void handStarted(TrucoEvent event)
 	{
 		System.out.println("Se disparo un hand started en " + this.getClass());
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	public void gameStarted(TrucoEvent event)
 	{
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	
 	public void endOfGame(TrucoEvent event)
 	{
 		Document doc = event.toXml();
 		super.sendXmlPackage(doc);
 	}
 	
 	public TrucoPlayer getAssociatedPlayer()
 	{
 		return player;
 	}
 	public void setRoom(RoomServer pieza)
 	{
 		this.pieza = pieza;
 		pieza.addRoomListener(this);
 	}
 	
 	public RoomServer getRoom()
 	{
 		return this.pieza;
 	}
 	
 	public void xmlReadLogin(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				user = element.getText();
 			}
 			if (aux.compareTo("Password") == 0)
 			{
 				//System.out.println("PASSWORD:"+element.getText());
 				password = element.getText();
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadLogin(child);
 			}
 			if (aux.compareTo("Login") == 0)
 			{
 				//Hace la autenticacion contra la base de datos
 				//DbOoperation.authenticatePlayer(user,password);
 				System.out.println(
 				"Dentro del Communicator Client, recibi un login request de: ");
 				System.out.println("Username: " + user);
 				System.out.println("Password: " + password);
 				
 				try
 				{
 					pieza.login(user, password, this);
 				} catch (py.edu.uca.fcyt.toluca.LoginFailedException lfe)
 				{
 					System.out.println("hubo un error en el login");
 					System.out.println("enviar uno paquete indicando el error");
 					//No se pudo logear
 				}
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
 	public void chatMessageRequested(
 	ChatPanelContainer chatPanelContainer,
 	TrucoPlayer player,
 	String htmlMessage)
 	{
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	
 	//   public void createTableRequested(RoomEvent ev) { }
 	
 	/** <p>
 	 * Does ...
 	 * </p><p>
 	 *
 	 * </p>
 	 *
 	 */
 	public void gameStartRequested()
 	{
 		System.out.println(
 		"En el CS.  Esto no se debera ejecutar.  recibi un GameStartRequested");
 	}
 	
 	public void chatMessageSent(
 	ChatPanelContainer cpc,
 	TrucoPlayer player,
 	String htmlMessage)
 	{
 		System.out.println("Enviando msg de chat del jug: " + player.getName());
 		Document doc;
 		doc = super.xmlCreateChatMsg(cpc, player, htmlMessage);
 		super.sendXmlPackage(doc);
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
 		new Throwable("nada implementado aun :-(").printStackTrace(System.out);
 	}
 	
 	public void xmlReadCreateTable(Object o)
 	{
 		String aux;
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				user = element.getAttributeValue("id");
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadCreateTable(child);
 			}
 			if (aux.compareTo("CreateTable") == 0)
 			{
 				System.out.println(
 				"Dentro del Communicator Server. Player = "
 				+ user
 				+ "/"
 				+ player.getName());
 				//pieza.createTable(new TrucoPlayer(user));
 				TrucoPlayer auxi=pieza.getPlayer(user);
 				pieza.createTable(auxi);
 				
 				
 				for(Enumeration e=getTables().elements();e.hasMoreElements();)
 				{
 					TableServer tabela=(TableServer)e.nextElement();
 					System.out.println("*****#####*****La tabla dentro de create table\n"+tabela);	
 		
 				}	
 				
 				
 				
 			}
 		}
 	}
 	
 	/** <p>
 	 * Does ...
 	 * </p><p>
 	 *
 	 * </p><p>
 	 *
 	 * @param ev ...
 	 * </p>
 	 */
 	public void tableCreated(RoomEvent ev)
 	{
 		
 		System.out.println("No hay nada: " + ev.getTabless().toArray().length);
 		TableServer ts = (TableServer) ((ev.getTabless().toArray())[0]);
 		ts.addTableServerListener(this);
 		// Agregamos al Hash de Tablas
 		System.out.println("getTables nulo: " + (getTables() == null));
 		getTables().put(String.valueOf(ev.getTableNumber()), ts);
 		System.out.println("Se agreg una tabla. soy: " + getClass().getName());
 		Document doc;
 		doc = xmlCreateTableCreated(ev);
 		System.out.println(
 		"Antes de mandar el XML en el CREATE TABLE DEL SERVA");
 		super.sendXmlPackage(doc);
 	}
 	
 	String tableid;
 	public void xmlReadTableJoinRequest(Object o)
 	{
 		String aux;
 		System.out.println("Leyendo el paquete");
 		if (o instanceof Element)
 		{
 			Element element = (Element) o;
 			aux = element.getName();
 			
 			if (aux.compareTo("Table") == 0)
 			{
 				//System.out.println("MESSAGE:"+element.getText());
 				tableid = element.getAttributeValue("id");
 				System.out.println("El table id es" + tableid);
 			}
 			if (aux.compareTo("Player") == 0)
 			{
 				//System.out.println("PLAYER:"+element.getText());
 				userAux = element.getAttributeValue("name");
 			}
 			List children = element.getContent();
 			Iterator iterator = children.iterator();
 			while (iterator.hasNext())
 			{
 				Object child = iterator.next();
 				xmlReadTableJoinRequest(child);
 			}
 			if (aux.compareTo("TableJoinRequest") == 0)
 			{
 				//Chatpanel.showChatMessage(user,message);
 				/* TableServer tstmp = (TableServer)getTables().get(tableid);
 				 if (tstmp == null) {
 					 System.out.println("TABLA NUUUUUUUUUUUUUUULA :(( :((!!!!");
 				 } else {
 					 TrucoPlayer tptmp = pieza.getPlayer(userAux);
 					 if (tptmp == null) {
 						 System.out.println("Player NUUUUUUUUUUUUULO :(( :(( ::((@!!!!!");
 					 } else {
 						 tstmp.addPlayer(tptmp);
 					 }
 				 }*/
 				 System.out.println("Dentro de table join request imp datos tablas\n");
 				 py.edu.uca.fcyt.util.HashUtils.imprimirHash(pieza.getHashTable());
 				 py.edu.uca.fcyt.util.HashUtils.imprimirHash(pieza.getHashPlayers());
 				 System.out.println("La tabla"+pieza.getHashTable().get(new Integer(tableid)));
 				 System.out.println("El playername"+userAux);
 				 System.out.println("El player en verdad: "+pieza.getPlayer(userAux));
 				try
 				{
 					
 					//TableServer tstmp = (TableServer) getTables().get(tableid);
 					//la linea de arriba fue cambiada por la de abajo. Powered Cricco
 					TableServer tstmp=(TableServer)(pieza.getHashTable().get(new Integer(tableid)));
 					System.out.println("La tabla a la cual se va adherir es\n"+tstmp);
 					TrucoPlayer tptmp = pieza.getPlayer(userAux);
 					
 					RoomEvent re = new RoomEvent();
 					re.setPlayer(tptmp);
 					re.setTableServer(tstmp);
 					re.setType(RoomEvent.TYPE_TABLE_JOINED);
 					pieza.joinTable(re);
 					
 				} catch (NullPointerException npe)
 				{
 					System.out.println("TABLA NUUUUUUUUUUUUUUULA :(( :((!!!!");
 					System.out.println(
 					"Player NUUUUUUUUUUUUULO :(( :(( ::((@!!!!!");
 					npe.printStackTrace(System.out);
 				}
 				
 			}
 		}
 	}
 	public Document xmlCreateTableCreated(RoomEvent re)
 	{
 		System.out.println("Cricco quiere ver su pakochi");
 		Element ROOT = new Element("TableCreated");
 		
 		Element PLAYER = new Element("Player");
 		Vector Jugadores = (Vector) re.getPlayerss();
 		TrucoPlayer jug = (TrucoPlayer) (Jugadores.get(0));
 		PLAYER.setAttribute("name", jug.getName());
 		Element TABLE = new Element("Table");
 		TABLE.setAttribute("id", String.valueOf(re.getTableNumber()));
 		
 		ROOT.addContent(PLAYER);
 		ROOT.addContent(TABLE);
 		
 		Document doc = new Document(ROOT);
 		return doc;
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
 	public static void main(String[] args)
 	{
 					RoomEvent te=new RoomEvent();
 					
 					
        				Vector jugadores=new Vector();
 					jugadores.add(new TrucoPlayer("Dani",105));
 					jugadores.add(new TrucoPlayer("Cricco",5));
 					jugadores.add(new TrucoPlayer("Crey",108));
 					
 					CommunicatorServer cc=new CommunicatorServer();
 					te.setPlayers(jugadores);
 					
 					Vector mesas=new Vector();
 					TableServer mesa1=new TableServer();
 					mesa1.addPlayer(new TrucoPlayer("Jose",5));
 					mesa1.addPlayer(new TrucoPlayer("Menganito",8));
 					
 					
 					mesas.add(mesa1);
 					te.setTabless(mesas);
 					
 					TableServer mesa2=new TableServer();
 					mesa2.addPlayer(new TrucoPlayer("Juan",5));
 					mesa2.addPlayer(new TrucoPlayer("Fulanito",8));
 					mesas.add(mesa2);
 					mesa2.setTableNumber(5);
 					te.setTabless(mesas);
 					Document doc=cc.xmlCreateLoginOk(te);
 														
 					cc.impXml(doc);
 					
 					CommunicatorClient cc1 = new CommunicatorClient();
 					System.out.println("************************");
 					List children = doc.getContent();
 					Iterator iterator = children.iterator();
 					Object child = iterator.next();
 					Element element = (Element) child;
 					String aux = element.getName();
 				
 					cc1.xmlReadLoginOk(child);
 					System.out.println("El table es: "+mesa1);
 					
 					 //cc.cabecera(doc);
 	}
 }
