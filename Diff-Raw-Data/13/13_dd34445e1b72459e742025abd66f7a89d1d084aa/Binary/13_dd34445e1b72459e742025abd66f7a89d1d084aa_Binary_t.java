 /*******************************************************************************
  * Copyright (c) 2013 Juuso Vilmunen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Juuso Vilmunen - initial API and implementation
  ******************************************************************************/
 package waazdoh.client;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import waazdoh.cutils.HashSource;
 import waazdoh.cutils.JBeanResponse;
 import waazdoh.cutils.MCRC;
 import waazdoh.cutils.MLogger;
 import waazdoh.cutils.MStringID;
 import waazdoh.cutils.UserID;
 import waazdoh.cutils.xml.JBean;
 import waazdoh.service.CMService;
 
 public final class Binary implements HashSource {
	private static final int DEFAULT_BYTEARRAYSIZE = 1000;
 	private Byte[] bytes = new Byte[10];
 	private int bytesindex = 0;
 	//
 	private int length = -1;
 	private MCRC crc;
 	private long timestamp;
 	//
 	private MLogger log = MLogger.getLogger(this);
 	private MCRC storedcrc;
 	private List<BinaryListener> listeners;
 	//
 	public final static String TYPE_STREAM = "audiostream";
 	private String type = TYPE_STREAM;
 	private UserID creatorid;
 	private String version;
 	private String comment = "";
 	private long usedtime;
 	private boolean ready;
 	private CMService service;
 	private String extension;
 	private MBinaryID id;
 
 	private static int count = 0;
 
 	public Binary(CMService service, String comment, String extension) {
 		this.service = service;
 		this.id = new MBinaryID();
 		creatorid = service.getUserID();
 		//
 		if (service != null) {
 			this.creatorid = service.getUserID();
 		}
 		version = WaazdohInfo.version;
 		this.extension = extension;
 		this.comment = comment;
 
 		used();
 	}
 
 	public Binary(MBinaryID streamid, CMService service) {
 		count++;
 
 		this.id = streamid;
 		this.service = service;
 		//
 		loadFromService(streamid);
 		//
 		used();
 	}
 
 	@Override
 	public String getHash() {
 		return getBean().getContentHash();
 	}
 
 	public int getMemoryUsage() {
 		int total = bytes.length;
 		return total;
 	}
 
 	private void init() {
 		resetCRC();
 	}
 
 	public String getFilename() {
 		return getID().toString() + "." + extension;
 	}
 
 	public synchronized boolean load(InputStream is) {
 		try {
 			log.info("" + this + " loading from inputstream " + is);
 			bytes = new Byte[1000];
 			byte[] nbytes = new byte[1000];
 			// @
 			while (true) {
 				int count = is.read(nbytes);
 				if (count <= 0) {
 					break;
 				}
 				add(nbytes, count);
 			}
 			crc = null;
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public synchronized int addAt(int index, byte[] nbytes) {
 		ensureSize(index + nbytes.length);
 		int overwritecount = 0;
 
 		for (byte b : nbytes) {
 			if (bytes[index] != null) {
 				overwritecount++;
 			}
 
 			bytes[index++] = b;
 		}
 		if (index > bytesindex) {
 			bytesindex = index;
 		}
 		//
 		resetCRC();
 		//
 		return overwritecount;
 	}
 
 	public synchronized void add(byte[] nbytes, int length) {
 		ensureSize(bytesindex + length);
 		for (int i = 0; i < length; i++) {
 			bytes[bytesindex++] = nbytes[i];
 		}
 		resetCRC();
 	}
 
 	private synchronized void ensureSize(int i) {
 		while (bytes.length < i) {
 			try {
 				int nlength = (int) (i * 1.1 + 10);
 				Byte[] nbytes = new Byte[nlength];
 				System.arraycopy(bytes, 0, nbytes, 0, bytes.length);
 				bytes = nbytes;
 				log.debug("binary using bytes " + bytes.length);
 			} catch (OutOfMemoryError memoryerror) {
 				log.error(memoryerror);
 				log.error("Out of memory with index " + i + " size:"
 						+ bytes.length);
 				log.info("ensureSize " + memoryerror);
 				try {
 					this.wait(10000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 					log.info("" + e);
 				}
 			}
 		}
 	}
 
 	public synchronized void add(byte[] fsamples) {
 		used();
 
 		ensureSize(bytesindex + fsamples.length);
 		//
 		for (int i = 0; i < fsamples.length; i++) {
 			bytes[bytesindex++] = fsamples[i];
 		}
 		log.info("bytes size " + bytesindex);
 		resetCRC();
 	}
 
 	public synchronized void add(Byte b) {
 		ensureSize(bytesindex + 100);
 		bytes[bytesindex++] = b;
 		resetCRC();
 	}
 
 	// public FloatStream(JBean b, CMService service) {
 	// this.service = service;
 	// load(b);
 	// }
 	private void load(JBean b) {
 		if (b.get("binary") != null) {
 			b = b.get("binary");
 			id = new MBinaryID(b.getAttribute("id"));
 		}
 		//
 		this.length = b.getIntValue("length");
 		this.storedcrc = new MCRC(b.getLongValue("crc"));
 		this.creatorid = new UserID(b.getValue("creator"));
 		this.version = b.getValue("version");
 		this.extension = b.getValue("extension");
 		this.comment = b.getValue("comment");
 	}
 
 	private synchronized boolean loadFromService(MStringID pid) {
 		JBeanResponse b = service.read(pid);
 		if (b != null && b.isSuccess()) {
 			log.info("loading Binary " + b);
 			load(b.getBean().find("binary"));
 			return true;
 		} else {
 			log.info("Service read " + pid + " failed");
 			return false;
 		}
 	}
 
 	public void publish() {
 		save();
 		service.publish(getID());
 	}
 
 	public void save() {
 		creatorid = service.getUserID();
 
 		JBean bean = getBean();
 		bean.setAttribute("id", getID().toString());
 		//
 		service.write(getID(), bean);
 	}
 
 	public synchronized void fillWithNaN(int length) {
 		used();
 
		bytes = new Byte[DEFAULT_BYTEARRAYSIZE];
 	}
 
 	@Override
 	public String toString() {
 		return "Binary[" + super.toString() + ":" + getID() + "][no:" + count
 				+ "][" + length() + "][scrc:" + storedcrc + "][crc:" + crc
 				+ "][" + comment + "]";
 	}
 
 	public boolean isOK() {
 		if (id == null || storedcrc == null || creatorid == null) {
 			return false;
 		}
 		return true;
 	}
 
 	public JBean getBean() {
 		JBean b = new JBean("binary");
 		//
 		b.addValue("length", "" + length);
 		b.addValue("crc", "" + currentCRC().getValue());
 		b.addValue("creator", "" + creatorid);
 		b.addValue("version", version);
 		b.addValue("comment", "" + comment);
 		b.addValue("extension", extension);
 		return b;
 	}
 
 	public synchronized boolean save(OutputStream os) {
 		try {
 			BufferedOutputStream bos = new BufferedOutputStream(os);
 			for (int i = 0; i < bytesindex; i++) {
 				bos.write(bytes[i]);
 			}
 			bos.close();
 			os.close();
 			return true;
 		} catch (IOException e) {
 			log.error(e);
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public synchronized int read(int index, byte bs[]) {
 		used();
 
 		int bsindex = 0;
 		while (bsindex < bs.length && bsindex < bytesindex) {
 			bs[bsindex] = bytes[index];
 			bsindex++;
 		}
 		return bsindex;
 	}
 
 	public int length() {
 		return length;
 	}
 
 	public synchronized Byte get(int isample) {
 		used();
 
 		if (isample < bytesindex) {
 			return bytes[isample];
 		} else {
 			return null;
 		}
 	}
 
 	private void used() {
 		this.usedtime = System.currentTimeMillis();
 	}
 
 	public synchronized void set(int isample, byte[] f) {
 		ensureSize(isample + f.length);
 		for (byte b : f) {
 			bytes[isample++] = b;
 		}
 		if (isample > bytesindex) {
 			bytesindex = isample;
 		}
 	}
 
 	synchronized MCRC currentCRC() {
 		MCRC ncrc = new MCRC(bytes, bytesindex);
 		return ncrc;
 	}
 
 	public MBinaryID getID() {
 		return id;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public synchronized MCRC getCRC() {
 		if (crc == null) {
 			crc = currentCRC();
 		}
 		return crc;
 	}
 
 	public synchronized void resetCRC() {
 		if (crc != null) {
 			crc = null;
 			//
 			log.debug("resetCRC " + id);
 		}
 		timestamp = System.currentTimeMillis();
 	}
 
 	public synchronized boolean isReady() {
 		boolean currentlyready = getCRC().equals(storedcrc);
 		if (currentlyready) {
 			if (!ready) {
 				ready = currentlyready;
 				log.info("" + this + " READY with " + getCRC());
 				fireReady();
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Binary) {
 			Binary bin = (Binary) obj;
 			if (!bin.getID().equals(getID()))
 				return false;
 			if (!getCRC().equals(bin.getCRC()))
 				return false;
 			return true;
 		}
 		return false;
 	}
 
 	public void setReady() {
 		used();
 
 		storedcrc = currentCRC();
 		length = bytesindex;
 		fireReady();
 	}
 
 	private void fireReady() {
 		if (listeners != null) {
 			final Binary me = this;
 			List<BinaryListener> ls = this.listeners;
 			for (final BinaryListener listener : ls) {
 				log.info("notifying READY " + listener);
 				Thread t = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						listener.ready(me);
 					}
 				}, "Binary FireReady");
 				t.start();
 			}
 		}
 	}
 
 	public void addListener(BinaryListener listener) {
 		getListeners().add(listener);
 	}
 
 	private List<BinaryListener> getListeners() {
 		if (listeners == null) {
 			listeners = new LinkedList<BinaryListener>();
 		}
 		return listeners;
 	}
 
 	public synchronized byte[] asByteBuffer() {
 		used();
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		save(baos);
 		return baos.toByteArray();
 	}
 
 	public void clear() {
 		init();
 	}
 
 	public Byte[] getByteBuffer() {
 		used();
 
 		return bytes;
 	}
 
 	public int getBytesLength() {
 		used();
 
 		return bytesindex;
 	}
 
 	public boolean isUsed(int suggestedmemorytreshold) {
 		if (isReady()) {
 			long dtime = System.currentTimeMillis() - usedtime;
 			boolean ret = dtime < suggestedmemorytreshold;
 			log.info("isused " + ret + " " + dtime);
 			return ret;
 		} else {
 			return true;
 		}
 	}
 
 	public String getExtension() {
 		return extension;
 	}
 }
