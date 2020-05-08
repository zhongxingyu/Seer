 package messages;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import bson.BSONDocument;
 import bson.BSON;
 import java.lang.String;
 public class MessageParser {
 
 	public enum MessageType
 	{
 		OP_REPLY, 
 		OP_MSG,
 		OP_UPDATE,
 		OP_INSERT,
 		OP_QUERY,
 		OP_GET_MORE,
 		OP_DELETE,
 		OP_KILL_CURSORS,
 		OTHER
 	}
 
 	public static MessageType getType(byte[] msg)
 	{
 		int opCode = byteArrayToInt(msg, 12);
 		
 		switch(opCode)
 		{
 			//OP_REPLY 	1 	Reply to a client request. responseTo is set
 			case 1:
 				return MessageType.OP_REPLY;
 			//OP_MSG 	1000 	generic msg command followed by a string
 			case 1000:
 				return MessageType.OP_MSG;
 			//OP_UPDATE 	2001 	update document
 			case 2001:
 				return MessageType.OP_UPDATE;
 			//OP_INSERT 	2002 	insert new document
 			case 2002:
 				return MessageType.OP_INSERT;
 			//RESERVED 	2003 	formerly used for OP_GET_BY_OID
 			case 2003:
 				return MessageType.OTHER;
 			//OP_QUERY 	2004 	query a collection
 			case 2004:
 				return MessageType.OP_QUERY;
 			//OP_GET_MORE 	2005 	Get more data from a query. See Cursors
 			case 2005:
 				return MessageType.OP_GET_MORE;
 			//OP_DELETE 	2006 	Delete documents
 			case 2006:
 				return MessageType.OP_DELETE;
 			//OP_KILL_CURSORS 	2007 	Tell database client is done with a cursor
 			case 2007:
 				return MessageType.OP_KILL_CURSORS;
 			default:
 				return MessageType.OTHER;
 		}
 	}
 	
 	public static UpdateMessage ParseUpdateMessage(byte[] msg)
 	{
 		UpdateMessage updateMessage = new UpdateMessage();
 		
 		Index i = new Index();
 		updateMessage.header = getHeader(msg, i);
 		
 		int zero = getInt(msg, i);//powinno byc zero
 		
 		updateMessage.fullCollectionName = getString(msg, i);
 		
 		updateMessage.flags = getInt(msg, i);
 
 		updateMessage.selector = getDocument(msg, i);
 		updateMessage.update = getDocument(msg, i);
 		
 		return updateMessage;
 	}
 	
 	public static InsertMessage ParseInsertMessage(byte[] msg)
 	{
 		InsertMessage insertMessage = new InsertMessage();
 		
 		Index i = new Index();
 		insertMessage.header = getHeader(msg, i);
 		
		
 		insertMessage.flags = getInt(msg, i);
 		
 		String fullCollectionName = getString(msg, i);
 		
 		insertMessage.fullCollectionName = fullCollectionName;
 		
 		List<BSONDocument> documents = new LinkedList<BSONDocument>();
 		
		//wywalam whilea poniewa nigdy si nie odpala (w klasie Index maxSize nie jest ustawiany) - Marek
		//while(i.checkIt())
		//{
 			documents.add(getDocument(msg, i));
		//}
 		
 		insertMessage.documents = new BSONDocument[documents.size()];
 		
 		Iterator<BSONDocument> iterator = documents.iterator();
 		for(int j = 0; iterator.hasNext(); j++)
 		{
 			insertMessage.documents[j] = iterator.next();
 		}
 		
		
		//tutaj powinienem obrobic jeszcze adres do dokumentu / dokumentow ale nie wiem jak to zrobic
		
 		return insertMessage; 
 	}
 	
 	public static QueryMessage ParseQueryMessage(byte[] msg)
 	{
 		QueryMessage queryMessage = new QueryMessage();
 		Index i = new Index();
 		queryMessage.header = getHeader(msg, i);
 		
 		queryMessage.flags = getInt(msg, i);                  // bit vector of query options.  See below for details.
 		queryMessage.fullCollectionName = getString(msg, i); // "dbname.collectionname"
 		queryMessage.numberToSkip = getInt(msg, i);          // number of documents to skip
 		queryMessage.numberToReturn = getInt(msg, i);        // number of documents to return
 	                                     //  in the first OP_REPLY batch
 		queryMessage.query = getDocument(msg, i);                 // query object.  See below for details.
 		if(i.checkIt())
 		{
 			queryMessage.returnFieldSelector = getDocument(msg, i);// Optional. Selector indicating the fields
 		}
 		
 		return new QueryMessage();
 	}
 	
 	public static GetMoreMessage ParseGetMoreMessage(byte[] msg)
 	{
 		GetMoreMessage getMoreMessage = new GetMoreMessage();
 		Index i = new Index();
 		getMoreMessage.header = getHeader(msg, i);
 		int zero = getInt(msg, i); //nie wiem co z tym zerem
 		getMoreMessage.fullCollectionName = getString(msg, i); // "dbname.collectionname"
 		getMoreMessage.numberToReturn = getInt(msg, i);        // number of documents to return
 		getMoreMessage.cursorID = getInt64(msg, i);
 		return new GetMoreMessage();
 	}
 	
 	public static DeleteMessage ParseDeleteMessage(byte[] msg)
 	{
 		DeleteMessage deleteMessage = new DeleteMessage();
 		Index i = new Index();
 		deleteMessage.header = getHeader(msg, i);
 		int zero = getInt(msg, i); //nie wiem co z tym zerem
 		deleteMessage.fullCollectionName = getString(msg, i); // "dbname.collectionname"
 		deleteMessage.flags = getInt(msg, i);                  // bit vector of query options.  See below for details
 		deleteMessage.selector = getDocument(msg, i);
 
 		return new DeleteMessage();
 	}
 	
 	public static KillCursorsMessage ParseKillCursorsMessage(byte[] msg)
 	{
 		KillCursorsMessage killCursorsMessage = new KillCursorsMessage();
 		Index i = new Index();
 		killCursorsMessage.header = getHeader(msg, i);
 		int zero = getInt(msg, i); //nie wiem co z tym zerem
 		int numberOfCursorIDs = getInt(msg, i);
 		killCursorsMessage.numberOfCursorIDs = numberOfCursorIDs;
 		killCursorsMessage.cursorIDs = new long[numberOfCursorIDs];
 		
 		for(int temp = 0; temp<numberOfCursorIDs; temp++)
 		{
 			killCursorsMessage.cursorIDs[temp] = getInt64(msg, i);
 		}
 			
 		return new KillCursorsMessage();
 	}
 
 	public static GenericMessage ParseGenericMessage(byte[] msg)
 	{
 		GenericMessage genericMessage = new GenericMessage();
 		Index i = new Index();
 		genericMessage.header = getHeader(msg, i);
 		genericMessage.message = getString(msg,i);
 		return new GenericMessage();
 	}
 	
 	public static ReplyMessage ParseReplyMessage(byte[] msg)
 	{
 		ReplyMessage replyMessage = new ReplyMessage();
 		Index i = new Index();
 		replyMessage.header = getHeader(msg, i);
 		replyMessage.flags = getInt(msg,i);
 		replyMessage.fullCollectionName = getString(msg, i);
 		replyMessage.numberToSkip = getInt(msg, i);          // number of documents to skip
 		replyMessage.numberToReturn = getInt(msg, i);        // number of documents to return
 		return new ReplyMessage();
 	}
 	
 	//prywatne funkcje poni�ej
 	
 	private static MessageHeader getHeader(byte[] msg, Index i)
 	{
 		 int messageLength = getInt(msg, i);
 		 int requestID = getInt(msg, i);
 		 int responceTo = getInt(msg, i);
		 int opCode = getInt(msg, i);	
 		
 		return new MessageHeader(messageLength, requestID, responceTo, opCode);
 	}
 	
 	private static int getInt(byte[] b, Index i)
 	{
 		int value = byteArrayToInt(b, i.getValue());
 		i.move(4);
 		return value;
 	}
 	
 	private static long getInt64(byte[] b, Index i)
 	{
 		int value = byteArrayToInt64(b, i.getValue());
 		i.move(8);
 		return value;
 	}
 	
 	private static String getString(byte[] b, Index i)
 	{
 		
 		int from = i.getValue();
 		int to = from;
 		int change = 0;
 		while(b[to] != 0)
 		{
 			to = to + 1;
 			change = change + 1;
 			if(b.length <= to)
 			{
 				break;
 				//b�ont, doda� wyj�tek
 			}
 		}
 		
 		to = to - 1;
 		
 		String string = null;
 		try
 		{
 			string = new String(Arrays.copyOfRange(b, from, to));
 		}
 		catch(Exception e)
 		{
 		//nic tu nie ma 
 		}
 		
 		i.move(change + 1);
 		
 		return string;
 	}
 	
 	private static byte[] byteSubarray(byte[] msg, int from, int count)
 	{
 		return Arrays.copyOfRange(msg, from, from + count - 1);
 	}
 	
 	private static BSONDocument getDocument(byte[] msg, Index i)
 	{
 		int sizeOfDocument = getInt(msg, i);
 		i.move(-4);//trzeba sie cofnac
 		
 		byte[] documentSource = byteSubarray(msg, i.getValue(), sizeOfDocument);
 		
 		BSONDocument parsedDocument = new BSONDocument();
 		BSON.parseBSON(documentSource, parsedDocument);//tu nastepuje parsowanie
 		
 		
 		i.move(sizeOfDocument);
 		return parsedDocument;
 	}
 	private static int byteArrayToInt(byte[] b, int from)
 	{
 		return   b[from + 0] & 0xFF |
 				(b[from + 1] & 0xFF) << 8 |
 				(b[from + 2] & 0xFF) << 16 |
 				(b[from + 3] & 0xFF) << 24;
 	}
 	
 	private static int byteArrayToInt64(byte[] b, int from)
 	{
 		return   b[from + 0] & 0xFF |
 				(b[from + 1] & 0xFF) << 8 |
 				(b[from + 2] & 0xFF) << 16 |
 				(b[from + 2] & 0xFF) << 24 |
 				(b[from + 1] & 0xFF) << 32 |
 				(b[from + 2] & 0xFF) << 40 |
 				(b[from + 1] & 0xFF) << 48 |
 				(b[from + 2] & 0xFF) << 56;
 	}
 
 }
