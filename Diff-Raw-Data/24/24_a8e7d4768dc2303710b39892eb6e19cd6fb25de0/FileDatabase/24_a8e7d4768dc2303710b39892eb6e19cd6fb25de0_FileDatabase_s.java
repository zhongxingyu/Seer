 package edu.berkeley.gamesman.database;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The FileDatabase is a database designed to write directly to a local file.
  * The file format is not well defined at the moment, perhaps this should be
  * changed later.
  * 
  * @author Steven Schlansker
  */
 public class FileDatabase extends Database {
 
 	protected File myFile;
 
 	protected RandomAccessFile fd;
 
 	long offset;
 
 	@Override
 	public synchronized void close() {
 		try {
 			fd.close();
 		} catch (IOException e) {
 			Util.warn("Error while closing input stream for database: " + e);
 		}
 	}
 
 	@Override
 	public synchronized void flush() {
 		try {
 			fd.getFD().sync();
 			fd.getChannel().force(true);
 		} catch (IOException e) {
 			Util.fatalError("Error while writing to database: " + e);
 		}
 	}
 
 	@Override
 	public synchronized Record getValue(BigInteger loc) {
 		try {
 			fd.seek(loc.longValue() + offset);
 			Record v = Record.readStream(conf, fd);
 			return v;
 		} catch (IOException e) {
 			Util.fatalError("IO Error: " + e);
 		}
 		Util.fatalError("WTF");
 		return null; // Not reached
 	}
 
 	@Override
 	protected synchronized void initialize(String loc) {
 
 		boolean previouslyExisted;
 
 		try {
 			myFile = new File(new URI(loc));
 			Util.debug(DebugFacility.Database, "Opened DB: "+myFile);
 		} catch (URISyntaxException e1) {
 			Util.fatalError("Could not open URI " + loc + ": " + e1);
 		}
 
 		previouslyExisted = myFile.exists();
 
 		try {
 			fd = new RandomAccessFile(myFile, "rw");
 		} catch (FileNotFoundException e) {
 			Util.fatalError("Could not create/open database: " + e);
 		}
 
 		try {
 			fd.seek(0);
 		} catch (IOException e) {
 			Util.fatalError("IO error while seeking header: " + e);
 		}
 		if (previouslyExisted) {
 			try {
 				int headerLen = fd.readInt();
 				byte[] header = new byte[headerLen];
 				fd.readFully(header);
 				if(conf == null)
 					conf = Configuration.configurationFromBytes(header);
				//Util.assertTrue(new String(header).equals(conf
				//		.getConfigString()),
				//		"File database has wrong header; expecting \""
				//				+ conf.getConfigString() + "\" got \""
				//				+ new String(header) + "\"");
				if(!new String(header).equals(conf.getConfigString()))
					Util.warn("File database headers do not match, proceed at your own risk!");
 
 			} catch (IOException e) {
 				Util.fatalError("IO error while checking header: " + e);
 			}
 		} else {
 			if(conf == null)
 				Util.fatalError("You must specify a configuration if the database is to be created");
 			try {
 				fd.writeInt(conf.getConfigString().length());
 				fd.write(conf.getConfigString().getBytes());
 			} catch (IOException e) {
 				Util.fatalError("IO error while creating header: " + e);
 			}
 		}
 		
 		Util.assertTrue(Record.bitlength(conf) <= 8,
 		"FileDatabase can only store 8 bits per record for now"); // TODO: FIXME
 		
 		try {
 			offset = fd.getFilePointer();
 		} catch (IOException e) {
 			Util.fatalError("IO error while getting file pointer: " + e);
 		}
 	}
 
 	@Override
 	public synchronized void setValue(BigInteger loc, Record value) {
 		try {
 			fd.seek(loc.longValue() + offset);
 			value.writeStream(fd);
 			//Util.debug("write "+loc);
 		} catch (IOException e) {
 			Util.fatalError("IO Error: " + e);
 		}
 	}
 
 }
