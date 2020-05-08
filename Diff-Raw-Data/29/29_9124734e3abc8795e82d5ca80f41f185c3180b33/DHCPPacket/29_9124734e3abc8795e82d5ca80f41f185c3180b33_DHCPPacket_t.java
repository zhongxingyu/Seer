 package mnet;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Vector;
 
 public class DHCPPacket {
 	private byte op,
 				 htype,
 				 hlen,
 				 hops;
 	
 	private byte[] xid, //4 bytes
 				   secs, //2 bytes
 				   flags, //2 bytes (15 bits reserved for later!)
 				   ciaddr, //Client's IP Address (4 bytes)
 				   yiaddr, //your (client's) ip address (4 bytes)
 				   siaddr, //4 bytes
 				   giaddr, //4 bytes
 				   chaddr, //client's hardware address (16 bytes)
 				   sname,  //64 bytes
 				   file;   //128 bytes
 	private int padding = 0;
 	private Vector<Option> options; //variable number of options
 	
 	public DHCPPacket(){ 
 		options = new Vector<Option>();
 	}
 	public DHCPPacket setOp    (byte op){
 		if(op != Constants.BOOTREQUEST && op != Constants.BOOTREPLY)
 			throw new IllegalArgumentException();
 		this.op = op;
 		return this;
 	}
 	public DHCPPacket setHtype (byte i)     { this.htype = i    ; return this; }
 	public DHCPPacket setHlen  (byte hlen)  { this.hlen  = hlen ; return this; }
 	public DHCPPacket setHops  (byte hops)  { this.hops  = hops ; return this; }
 	public DHCPPacket setXid   (byte[] xid) { this.xid   = xid  ; return this; }
 	public DHCPPacket setXid   (int xid)    { this.xid   = ByteFactory.getIntAsBytes(xid)    ; return this; }
 	public DHCPPacket generateXid(){
 		this.xid = ByteFactory.getIntAsBytes(new Random().nextInt());
 		return this;
 	}
 	public DHCPPacket setSecs  (byte[] secs){ this.secs  = secs ; return this; }
 	public DHCPPacket setSecs  (int secs)   { this.secs  = ByteFactory.getIntAsBytes(secs, 2); return this; }
 	
 	public DHCPPacket setFlags (byte[] flags){
 		if(flags[1] != 0 || (flags[0] != 0 && flags[0] != 128))
 				throw new IllegalArgumentException();
 		this.flags = flags;
 		return this;
 	}
 	public DHCPPacket setBroadcastFlag(boolean bflag){
 		flags = new byte[2];
 		if(bflag)
 			flags[0] = (byte)128;
 		else
 			flags[0] = 0;
 		flags[1] = 0;
 		return this;
 	}
 	
 	public DHCPPacket setCiaddr (byte[] ciaddr){ this.ciaddr = ciaddr; return this; }
 	public DHCPPacket setCiaddr (int zero){ if(zero == 0) this.ciaddr = new byte[4]; return this;}
 	public DHCPPacket setYiaddr (byte[] yiaddr){ this.yiaddr = yiaddr; return this; }
 	public DHCPPacket setYiaddr (int zero){ if(zero == 0) this.yiaddr = new byte[4]; return this;}
 	public DHCPPacket setSiaddr (byte[] siaddr){ this.siaddr = siaddr; return this; }
 	public DHCPPacket setSiaddr (int zero){ if(zero == 0) this.siaddr = new byte[4]; return this;}
 	public DHCPPacket setGiaddr (byte[] giaddr){ this.giaddr = giaddr; return this; }
 	public DHCPPacket setGiaddr (int zero){ if(zero == 0) this.giaddr = new byte[4]; return this;}
 	public DHCPPacket setChaddr (byte[] chaddr){ this.chaddr = chaddr; return this; }
 	public DHCPPacket setChaddr (int zero){ if(zero == 0) this.chaddr = new byte[4]; return this;}
 	public DHCPPacket setSname  (byte[] sname) { this.sname  = sname ; return this; }
 	public DHCPPacket setSname  (int zero){ if(zero == 0) this.sname = new byte[64]; return this;}
 	public DHCPPacket setFile   (byte[] file)  { this.file   = file  ; return this; }
 	public DHCPPacket setFile   (int zero){ if(zero == 0) this.file = new byte[128]; return this;}
 	
 	public DHCPPacket addOption(byte code, byte len, byte[] data){
 		if(data.length != len) throw new IllegalArgumentException();
 		options.add(new Option(code, len, data)); return this;
 	}
 	public byte[]     getOption(byte code){
 		for(Option option : options){
 			if(option.code == code)
 				return option.data;
 		}
 		return null;
 	}
 	
 	public byte       getOp    (){ return op    ; }
 	public byte       getHtype (){ return htype ; }
 	public byte       getHlen  (){ return hlen  ; }
 	public byte       getHops  (){ return hops  ; }
 	public byte[]     getXid   (){ return xid   ; }
 	public byte[]     getSecs  (){ return secs  ; }
 	public byte[]     getCiaddr(){ return ciaddr; }
 	public byte[]     getYiaddr(){ return yiaddr; }
 	public byte[]     getSiaddr(){ return siaddr; }
 	public byte[]     getGiaddr(){ return giaddr; }
 	public byte[]     getChaddr(){ return chaddr; }
 	public byte[]     getSname (){ return sname ; }
 	public byte[]     getFile  (){ return file  ; }
 	public byte[]     getFlags (){ return flags ; }
 	public boolean    getBroadcastFlag(){ if(flags[0] == 0) return false; else return true; }
 	
 	public void       read (byte[] array){
 		ByteArrayInputStream in = new ByteArrayInputStream(array);
 		try {
 			read(in);
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 	public void       read (InputStream in) throws IOException{
 		byte[] temp = new byte[1];
 		in.read(temp); op    = temp[0];
 		in.read(temp); htype = temp[0];
 		in.read(temp); hlen  = temp[0];
 		in.read(temp); hops  = temp[0];
 		
 		xid    = new byte[4];  in.read(xid)   ;
 		secs   = new byte[2];  in.read(secs)  ;
 		flags  = new byte[2];  in.read(flags) ;
 		ciaddr = new byte[4];  in.read(ciaddr);
 		yiaddr = new byte[4];  in.read(yiaddr);
 		siaddr = new byte[4];  in.read(siaddr);
 		giaddr = new byte[4];  in.read(giaddr);
 		chaddr = new byte[16]; in.read(chaddr);
 		sname  = new byte[64]; in.read(sname) ;
 		file   = new byte[128];in.read(file)  ;
 		byte[] magiccookie = new byte[4];
 		in.read(magiccookie);
 		if(!Arrays.equals(magiccookie, Constants.magicCookie))
 			throw new Error("Bad Input Data. (Packet Data Corrupted)");
 		while(in.available() > 0){
 			in.read(temp);
 			byte code = temp[0];
			if(code == -1)
 				break;
 			in.read(temp);
 			byte len = temp[0];
 			byte[] data = new byte[len];
 			in.read(data);
 			options.add(new Option(code, len, data));
 		}
 		
 		padding = 0;
 		while(in.available() > 0){
 			in.read(temp);
 			if(temp[0] != 0)
 				throw new IOException("Wrong padding at end of options.");
 			padding++;
 		}
 	}
 	public byte[]     array(){
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		byte[] array = null;
 		try {
 			write(out);
 			array = out.toByteArray();
 		} catch (IOException e) { e.printStackTrace(); }
 		return array;
 	}
 	public void       write(OutputStream out) throws IOException{
 		out.write(new byte[]{op});
 		out.write(new byte[]{htype});
 		out.write(new byte[]{hlen});
 		out.write(new byte[]{hops});
 		out.write(xid);
 		out.write(secs);
 		out.write(flags);
 		out.write(ciaddr);
 		out.write(yiaddr);
 		out.write(siaddr);
 		out.write(giaddr);
 		out.write(chaddr);
 		out.write(sname);
 		out.write(file);
 		int len = 0;
 		out.write(Constants.magicCookie);
 		len += 4;
 		for(Option option : options){
 			out.write(new byte[]{option.code});
 			out.write(new byte[]{option.len});
 			out.write(option.data);
 			len += option.len + 2;
 		}
 		out.write(new byte[]{(byte)255});
 		len += 1;
 		while(len % 64 != 0){
 			out.write(new byte[]{0});
 			len += 1;
 		}
 	}
 	
 	private class Option{
 		byte code;
 		byte len;
 		byte[] data;
 		Option(byte code, byte len, byte[] data){
 			this.code = code;
 			this.len = len;
 			this.data = data;
 		}
 	}
 	
 }
