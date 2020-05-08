 package de.ptb.epics.eve.ecp1.intern;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import de.ptb.epics.eve.ecp1.intern.exceptions.AbstractRestoreECP1CommandException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongStartTagException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongTypeIdException;
 import de.ptb.epics.eve.ecp1.intern.exceptions.WrongVersionException;
 
 public class LiveDescriptionCommand implements IECP1Command {
 
 	public static final char COMMAND_TYPE_ID = 0x0012;
 	
 	private String liveDescription;
 	
 	public LiveDescriptionCommand() {
 		this.liveDescription = "";
 	}
 	
 	public LiveDescriptionCommand( final String liveDescription ) {
 		if( liveDescription == null ) {
 			throw new IllegalArgumentException( "The parameter 'liveDescription' must not be null!" );
 		}
 		this.liveDescription = liveDescription;
 	}
 	
 	public LiveDescriptionCommand( final byte[] byteArray ) throws IOException, AbstractRestoreECP1CommandException {
 		if( byteArray == null ) {
 			throw new IllegalArgumentException( "The parameter 'byteArray' must not be null!" );
 		}
 		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( byteArray );
 		final DataInputStream dataInputStream = new DataInputStream( byteArrayInputStream );
 		
 		final int startTag = dataInputStream.readInt(); 
 		if( startTag != IECP1Command.START_TAG ) {
 			throw new WrongStartTagException( byteArray, startTag );
 		}
 		
 		final char version = dataInputStream.readChar(); 
 		if( version != IECP1Command.VERSION ) {
 			throw new WrongVersionException( byteArray, version );
 		}
 		
 		final char commandTypeID = dataInputStream.readChar();
 		if( commandTypeID != LiveDescriptionCommand.COMMAND_TYPE_ID ) {
 			throw new WrongTypeIdException( byteArray, commandTypeID, LiveDescriptionCommand.COMMAND_TYPE_ID );
 		}
 		
 		final int length = dataInputStream.readInt();
 		
 		final int lenghtOfText = dataInputStream.readInt();
 		
 		if( lenghtOfText == 0xffffffff ) {
 			this.liveDescription = "";
 		} else {
 			final byte[] liveDescriptionStringBuffer = new byte[ lenghtOfText * 2 ]; 
 			dataInputStream.readFully( liveDescriptionStringBuffer );
 			this.liveDescription = new String( liveDescriptionStringBuffer, IECP1Command.STRING_ENCODING );
 		}
 		
 	}
 	
 	public byte[] getByteArray() throws IOException {
 		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		final DataOutputStream dataOutputStream = new DataOutputStream( byteArrayOutputStream );
 		
 		dataOutputStream.writeInt( IECP1Command.START_TAG );
 		dataOutputStream.writeChar( IECP1Command.VERSION );;
 		dataOutputStream.writeChar( LiveDescriptionCommand.COMMAND_TYPE_ID );
 		if( this.liveDescription.length() == 0 ) {
 			dataOutputStream.writeInt( 4 );
 			dataOutputStream.writeInt( 0xffffffff );
 		} else {
 			final byte[] stringByte = this.liveDescription.getBytes( IECP1Command.STRING_ENCODING );
 			dataOutputStream.writeInt( 4 + stringByte.length );
			dataOutputStream.writeInt( stringByte.length );
 			dataOutputStream.write( stringByte );
 		}
 		
 		dataOutputStream.close();
 		
 		return byteArrayOutputStream.toByteArray();
 	}
 	
 	public String getLiveDescription() {
 		return this.liveDescription;
 	}
 
 	public void setLiveDescription( final String liveDescription ) {
 		if( liveDescription == null ) {
 			throw new IllegalArgumentException( "The parameter 'liveDescription' must not be null!" );
 		}
 		this.liveDescription = liveDescription;
 	}
 	
 }
