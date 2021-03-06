 package haven;
 
 import java.net.*;
 import java.util.*;
 import java.io.*;
 
 public class Session {
 	public static final int PVER = 10;
 	
 	public static final int MSG_SESS = 0;
 	public static final int MSG_REL = 1;
 	public static final int MSG_ACK = 2;
 	public static final int MSG_BEAT = 3;
 	public static final int MSG_MAPREQ = 4;
 	public static final int MSG_MAPDATA = 5;
 	public static final int MSG_OBJDATA = 6;
 	public static final int MSG_OBJACK = 7;
 	public static final int MSG_CLOSE = 8;
 	public static final int OD_REM = 0;
 	public static final int OD_MOVE = 1;
 	public static final int OD_RES = 2;
 	public static final int OD_LINBEG = 3;
 	public static final int OD_LINSTEP = 4;
 	public static final int OD_SPEECH = 5;
 	public static final int OD_LAYERS = 6;
 	public static final int OD_DRAWOFF = 7;
 	public static final int OD_LUMIN = 8;
 	public static final int OD_AVATAR = 9;
 	public static final int OD_FOLLOW = 10;
 	public static final int OD_HOMING = 11;
 	public static final int OD_OVERLAY = 12;
 	public static final int OD_END = 255;
 	public static final int SESSERR_AUTH = 1;
 	public static final int SESSERR_BUSY = 2;
 	public static final int SESSERR_CONN = 3;
 	public static final int SESSERR_PVER = 4;
 	public static final int SESSERR_EXPR = 5;
 	
 	DatagramSocket sk;
 	InetAddress server;
 	Thread rworker, sworker, ticker;
 	int connfailed = 0;
 	String state = "conn";
 	int tseq = 0, rseq = 0;
 	LinkedList<Message> uimsgs = new LinkedList<Message>();
 	Map<Integer, Message> waiting = new TreeMap<Integer, Message>();
 	LinkedList<Message> pending = new LinkedList<Message>();
 	Map<Integer, ObjAck> objacks = new TreeMap<Integer, ObjAck>();
 	String username;
 	byte[] cookie;
 	final Map<Integer, Indir<Resource>> rescache = new TreeMap<Integer, Indir<Resource>>();
 	final Glob glob;
 	
 	@SuppressWarnings("serial")
 	public class MessageException extends RuntimeException {
 		public Message msg;
 		
 		public MessageException(String text, Message msg) {
 			super(text);
 			this.msg = msg;
 		}
 	}
 	
 	public Indir<Resource> getres(final int id) {
 		synchronized(rescache) {
 			Indir<Resource> ret = rescache.get(id);
 			if(ret != null)
 				return(ret);
 			ret = new Indir<Resource>() {
 				public int resid = id;
 				Resource res;
 					
 				public Resource get() {
 					if(res == null)
 						return(null);
 					if(res.loading)
 						return(null);
 					return(res);
 				}
 					
 				public void set(Resource r) {
 					res = r;
 				}
 				
 				public int compareTo(Indir<Resource> x) {
 					return((this.getClass().cast(x)).resid - resid);
 				}
 			};
 			rescache.put(id, ret);
 			return(ret);
 		}
 	}
 
 	private class ObjAck {
 		int id;
 		int frame;
 		long recv;
 		long sent;
 		
 		public ObjAck(int id, int frame, long recv) {
 			this.id = id;
 			this.frame = frame;
 			this.recv = recv;
 			this.sent = 0;
 		}
 	}
     
 	private class Ticker extends Thread {
 		public Ticker() {
 			super(Utils.tg(), "Server time ticker");
 			setDaemon(true);
 		}
 		
 		public void run() {
 			try {
 				while(true) {
 					long now, then;
 					then = System.currentTimeMillis();
 					glob.oc.tick();
 					now = System.currentTimeMillis();
 					if(now - then < 70)
 						Thread.sleep(70 - (now - then));
 				}
 			} catch(InterruptedException e) {}
 		}
 	}
 	
 	private class RWorker extends Thread {
 		boolean alive;
 		
 		public RWorker() {
 			super(Utils.tg(), "Session reader");
 			setDaemon(true);
 		}
 		
 		private void sendack(int seq) {
 			byte[] msg = {MSG_ACK, 0, 0};
 			Utils.uint16e(seq, msg, 1);
 			sendmsg(msg);
 		}
 		
 		private void gotack(int seq) {
 			synchronized(pending) {
 				for(ListIterator<Message> i = pending.listIterator(); i.hasNext(); ) {
 					Message msg = i.next();
 					if(msg.seq <= seq)
 						i.remove();
 				}
 			}
 		}
 		
 		private void getobjdata(Message msg) {
 			OCache oc = glob.oc;
 			while(msg.off < msg.blob.length) {
 				int fl = msg.uint8();
 				int id = msg.int32();
 				int frame = msg.int32();
 				if((fl & 1) != 0) {
 					oc.remove(id, frame - 1);
 				}
 				synchronized(oc) {
 					while(true) {
 						int type = msg.uint8();
 						if(type == OD_REM) {
 							oc.remove(id, frame);
 						} else if(type == OD_MOVE) {
 							Coord c = msg.coord();
 							oc.move(id, frame, c);
 						} else if(type == OD_RES) {
 							int resid = msg.uint16();
 							Message sdt;
 							if((resid & 0x8000) != 0) {
 								resid &= ~0x8000;
 								sdt = msg.derive(0, msg.uint8());
 							} else {
 								sdt = new Message(0);
 							}
 							oc.cres(id, frame, getres(resid), sdt);
 						} else if(type == OD_LINBEG) {
 							Coord s = msg.coord();
 							Coord t = msg.coord();
 							int c = msg.int32();
 							oc.linbeg(id, frame, s, t, c);
 						} else if(type == OD_LINSTEP) {
 							int l = msg.int32();
 							oc.linstep(id, frame, l);
 						} else if(type == OD_SPEECH) {
 							Coord off = msg.coord();
 							String text = msg.string();
 							oc.speak(id, frame, off, text);
 						} else if((type == OD_LAYERS) || (type == OD_AVATAR)) {
 							Indir<Resource> baseres = null;
 							if(type == OD_LAYERS)
 								baseres = getres(msg.uint16());
 							List<Indir<Resource>> layers = new LinkedList<Indir<Resource>>();
 							while(true) {
 								int layer = msg.uint16();
 								if(layer == 65535)
 									break;
 								layers.add(getres(layer));
 							}
 							if(type == OD_LAYERS)
 								oc.layers(id, frame, baseres, layers);
 							else
 								oc.avatar(id, frame, layers);
 						} else if(type == OD_DRAWOFF) {
 							Coord off = msg.coord();
 							oc.drawoff(id, frame, off);
 						} else if(type == OD_LUMIN) {
 							oc.lumin(id, frame, msg.coord(), msg.uint16(), msg.uint8());
 						} else if(type == OD_FOLLOW) {
 							int oid = msg.int32();
 							Coord off = Coord.z;
 							if(oid != -1)
 								off = msg.coord();
 							oc.follow(id, frame, oid, off);
 						} else if(type == OD_HOMING) {
 							int oid = msg.int32();
 							if(oid == -1) {
 								oc.homostop(id, frame);
 							} else if(oid == -2) {
 								Coord tgtc = msg.coord();
 								int v = msg.uint16();
 								oc.homocoord(id, frame, tgtc, v);
 							} else {
 								Coord tgtc = msg.coord();
 								int v = msg.uint16();
 								oc.homing(id, frame, oid, tgtc, v);
 							}
 						} else if(type == OD_OVERLAY) {
 							int resid = msg.uint16();
 							int olid = msg.int32();
 							oc.overlay(id, frame, getres(resid), olid);
 						} else if(type == OD_END) {
 							break;
 						} else {
 							throw(new MessageException("Unknown objdelta type: " + type, msg));
 						}
 					}
 					Gob g = oc.getgob(id, frame);
 					if(g != null)
 						g.frame = frame;
 				}
 				synchronized(objacks) {
 					if(objacks.containsKey(id)) {
 						ObjAck a = objacks.get(id);
 						a.frame = frame;
 						a.recv = System.currentTimeMillis();
 					} else {
 						objacks.put(id, new ObjAck(id, frame, System.currentTimeMillis()));
 					}
 				}
 				synchronized(sworker) {
 					sworker.notifyAll();
 				}
 			}
 		}
 		
 		private void handlerel(Message msg) {
 			if(msg.type == Message.RMSG_NEWWDG) {
 				synchronized(uimsgs) {
 					uimsgs.add(msg);
 				}
 			} else if(msg.type == Message.RMSG_WDGMSG) {
 				synchronized(uimsgs) {
 					uimsgs.add(msg);
 				}
 			} else if(msg.type == Message.RMSG_DSTWDG) {
 				synchronized(uimsgs) {
 					uimsgs.add(msg);
 				}
 			} else if(msg.type == Message.RMSG_MAPIV) {
 				glob.map.invalidate(msg.coord());
 			} else if(msg.type == Message.RMSG_GLOBLOB) {
 				glob.blob(msg);
 			} else if(msg.type == Message.RMSG_PAGINAE) {
 				glob.paginae(msg);
 			} else if(msg.type == Message.RMSG_RESID) {
 				int resid = msg.uint16();
 				String resname = msg.string();
 				int resver = msg.uint16();
 				synchronized(rescache) {
 					getres(resid).set(Resource.load(resname, resver));
 				}
 			} else if(msg.type == Message.RMSG_PARTY) {
 				glob.party.msg(msg);
 			} else if(msg.type == Message.RMSG_SFX) {
 				Indir<Resource> res = getres(msg.uint16());
 				double vol = ((double)msg.uint16()) / 256.0;
 				double spd = ((double)msg.uint16()) / 256.0;
				Audio.play(res);
 			} else {
 				throw(new MessageException("Unknown rmsg type: " + msg.type, msg));
 			}
 		}
 		
 		private void getrel(Message msg) {
 			int seq = msg.uint16();
 			msg = new Message(msg.uint8(), msg.blob, msg.off, msg.blob.length - msg.off);
 			if(seq == rseq) {
 				synchronized(uimsgs) {
 					handlerel(msg);
 					while(true) {
 						rseq = (rseq + 1) % 65536;
 						if(!waiting.containsKey(rseq))
 							break;
 						handlerel(waiting.get(rseq));
 						waiting.remove(rseq);
 					}
 				}
 				sendack(rseq - 1);
 				synchronized(Session.this) {
 					Session.this.notifyAll();
 				}
 			} else if(seq > rseq) {
 				waiting.put(seq, msg);
 			}
 		}
 		
 		public void run() {
 			try {
 				alive = true;
 				try {
 					sk.setSoTimeout(1000);
 				} catch(SocketException e) {
 					throw(new RuntimeException(e));
 				}
 				while(alive) {
 					DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
 					try {
 						sk.receive(p);
 					} catch(java.nio.channels.ClosedByInterruptException e) {
 						/* Except apparently Sun's J2SE doesn't throw this when interrupted :P*/
 						break;
 					} catch(SocketTimeoutException e) {
 						continue;
 					} catch(IOException e) {
 						throw(new RuntimeException(e));
 					}
 					if(!p.getAddress().equals(server))
 						continue;
 					Message msg = new Message(p.getData()[0], p.getData(), 1, p.getLength() - 1);
 					if(msg.type == MSG_SESS) {
 						if(state == "conn") {
 							int error = msg.uint8();
 							synchronized(Session.this) {
 								if(error == 0) {
 									state = "";
 								} else {
 									connfailed = error;
 									Session.this.close();
 								}
 								Session.this.notifyAll();
 							}
 						}
 					}
 					if(state != "conn") {
 						if(msg.type == MSG_SESS) {
 						} else if(msg.type == MSG_REL) {
 							getrel(msg);
 						} else if(msg.type == MSG_ACK) {
 							gotack(msg.uint16());
 						} else if(msg.type == MSG_MAPDATA) {
 							glob.map.mapdata(msg);
 						} else if(msg.type == MSG_OBJDATA) {
 							getobjdata(msg);
 						} else if(msg.type == MSG_CLOSE) {
 							synchronized(Session.this) {
 								state = "fin";
 							}
 							Session.this.close();
 						} else {
 							throw(new MessageException("Unknown message type: " + msg.type, msg));
 						}
 					}
 				}
 			} finally {
 				synchronized(Session.this) {
 					state = "dead";
 					Session.this.notifyAll();
 				}
 			}
 		}
 		
 		public void interrupt() {
 			alive = false;
 			super.interrupt();
 		}
 	}
 	
 	private class SWorker extends Thread {
 		
 		public SWorker() {
 			super(Utils.tg(), "Session writer");
 			setDaemon(true);
 		}
 		
 		public void run() {
 			try {
 				long to, last = 0, retries = 0;
 				while(true) {
 					
 					long now = System.currentTimeMillis();
 					if(state == "conn") {
 						if(now - last > 2000) {
 							if(++retries > 5) {
 								synchronized(Session.this) {
 									connfailed = SESSERR_CONN;
 									Session.this.notifyAll();
 									return;
 								}
 							}
 							Message msg = new Message(MSG_SESS);
 							msg.adduint16(PVER);
 							msg.addstring(username);
 							msg.addbytes(cookie);
 							sendmsg(msg);
 							last = now;
 						}
 						Thread.sleep(100);
 					} else {
 						to = 5000;
 						synchronized(pending) {
 							if(pending.size() > 0)
 								to = 60;
 						}
 						synchronized(objacks) {
 							if((objacks.size() > 0) && (to > 120))
 								to = 200;
 						}
 						synchronized(this) {
 							this.wait(to);
 						}
 						now = System.currentTimeMillis();
 						boolean beat = true;
 						/*
 						if((closing != -1) && (now - closing > 500)) {
 							Message cm = new Message(MSG_CLOSE);
 							sendmsg(cm);
 							closing = now;
 							if(++ctries > 5)
 								getThreadGroup().interrupt();
 						}
 						*/
 						synchronized(pending) {
 							if(pending.size() > 0) {
 								for(Message msg : pending) {
 									if(now - msg.last > 60) { /* XXX */
 										msg.last = now;
 										sendmsg(msg);
 									}
 								}
 								beat = false;
 							}
 						}
 						synchronized(objacks) {
 							Message msg = null;
 							for(Iterator<ObjAck> i = objacks.values().iterator(); i.hasNext();) {
 								ObjAck a = i.next();
 								boolean send = false, del = false;
 								if(now - a.sent > 200)
 									send = true;
 								if(now - a.recv > 120)
 									send = del = true;
 								if(send) {
 									if(msg == null)
 										msg = new Message(MSG_OBJACK);
 									msg.addint32(a.id);
 									msg.addint32(a.frame);
 									a.sent = now;
 								}
 								if(del)
 									i.remove();
 							}
 							if(msg != null)
 								sendmsg(msg);
 						}
 						if(beat) {
 							if(now - last > 5000) {
 								sendmsg(new byte[] {MSG_BEAT});
 								last = now;
 							}
 						}
 					}
 				}
 			} catch(InterruptedException e) {
 				for(int i = 0; i < 5; i++) {
 					synchronized(Session.this) {
 						if((state == "conn") || (state == "fin"))
 							break;
 					}
 					sendmsg(new Message(MSG_CLOSE));
 					long f = System.currentTimeMillis();
 					while(true) {
 						long now = System.currentTimeMillis();
 						if(now - f > 500)
 							break;
 						try {
 							Thread.sleep(500 - (now - f));
 						} catch(InterruptedException e2) {}
 					}
 				}
 			} finally {
 				ticker.interrupt();
 				rworker.interrupt();
 			}
 		}
 	}
 	
 	public Session(InetAddress server, String username, byte[] cookie) {
 		this.server = server;
 		this.username = username;
 		this.cookie = cookie;
 		glob = new Glob(this);
 		try {
 			sk = new DatagramSocket();
 		} catch(SocketException e) {
 			throw(new RuntimeException(e));
 		}
 		rworker = new RWorker();
 		rworker.start();
 		sworker = new SWorker();
 		sworker.start();
 		ticker = new Ticker();
 		ticker.start();
 	}
 	
 	public void close() {
 		sworker.interrupt();
 	}
 	
 	public synchronized boolean alive() {
 		return(state != "dead");
 	}
 	
 	public void queuemsg(Message msg) {
 		Message rmsg = new Message(MSG_REL);
 		rmsg.adduint16(rmsg.seq = tseq);
 		rmsg.adduint8(msg.type);
 		rmsg.addbytes(msg.blob);
 		tseq = (tseq + 1) % 65536;
 		synchronized(pending) {
 			pending.add(rmsg);
 		}
 		synchronized(sworker) {
 			sworker.notify();
 		}
 	}
 	
 	public Message getuimsg() {
 		synchronized(uimsgs) {
 			if(uimsgs.size() == 0)
 				return(null);
 			return(uimsgs.remove());
 		}
 	}
 	
 	public void sendmsg(Message msg) {
 		byte[] buf = new byte[msg.blob.length + 1];
 		buf[0] = (byte)msg.type;
 		System.arraycopy(msg.blob, 0, buf, 1, msg.blob.length);
 		sendmsg(buf);
 	}
 	
 	public void sendmsg(byte[] msg) {
 		try {
 			sk.send(new DatagramPacket(msg, msg.length, server, 1870));
 		} catch(IOException e) {
 		}
 	}
 }
