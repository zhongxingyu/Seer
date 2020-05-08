 package crypto;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.util.ArrayList;
 
 import javax.naming.directory.SearchControls;
 
 import timer.Timer;
 import IO.DataIO;
 import IO.RW;
 
 
 /**
  * @author chenqian
  *
  */
 public class Gfunction implements RW{
 	
	public static long L = -((long)1 << 31), U = (long)1 << 31 - 1;
 	public byte[] r = "something".getBytes();
 	public byte[][] MHTree = null;
 	public byte[][][] GvalueU, GvalueL;
 	public byte[][] GvalueUs, GvalueLs;
 	public int[][] valueU, valueL;
 	public long v;
 	public int base, m;
 	public int whichSide; //0 both, -1 left, 1 right
 	public boolean isL;
 	
 	public Gfunction(long x, int _base, long _L, long _U) {
 		// TODO Auto-generated constructor stub
 		L = _L;
 		U = _U;
 		v = x;
 		base = _base;
 		m = (int)(Math.log(U - L) / Math.log(base));
 		whichSide = 0;
 		generateU();
 		generateL();
 		getDigest();
 	}
 	
 	public Gfunction(long x, int _base, long _L, long _U, int _whichSide) {
 		// TODO Auto-generated constructor stub
 		L = _L;
 		U = _U;
 		v = x;
 		base = _base;
 		m = (int)(Math.log(U - L) / Math.log(base));
 		whichSide = _whichSide;
 		generateU();
 		generateL();
 		getDigest();
 	}
 	
 	public Gfunction(long x, int _base) {
 		// TODO Auto-generated constructor stub
 		v = x;
 		base = _base;
 		m = (int)(Math.log(U - L) / Math.log(base));
 		whichSide = 0;
 		generateU();
 		generateL();
 		getDigest();
 	}
 	
 	public Gfunction() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public Gfunction(int _base) {
 		base = _base;
 		// TODO Auto-generated constructor stub
 	}
 	
 	public static byte[] hashXmore1times(byte[] s, int m){
 		if(m < 0)return null;
 		byte[] ans = s;
 		while(m -- >= 0){
 			ans = Hasher.hashBytes(ans);
 		}
 		return ans;
 	}
 	
 	public static byte[] hashXtimes(byte[] s, int m){
 		if(m < 0)return null;
 		byte[] ans = s;
 		while(m -- > 0){
 			ans = Hasher.hashBytes(ans);
 		}
 		return ans;
 	}
 	
 	public void generateU(){
 		GvalueUs = new byte[m + 1][];
 		GvalueU = new byte[m + 1][m + 1][];
 		valueU = new int[m + 1][m + 1];
 		if(whichSide >= 0){
 			for(int i = 0; i < m; i++){
 				long t = U - v;
 				boolean found = false;
 				//long t = U - x - 1; //if '=' is not allowed
 				for(int j = 0; j < m + 1; j++){
 					if(j == 0){
 						valueU[i][j] = (int) (t % base + base);
 					}else if(j <= i){
 						valueU[i][j] = (int) (t % base + base - 1);
 					}else if( j == i + 1){
 						valueU[i][j] = (int) (t % base - 1);
 					}else{
 						valueU[i][j] = (int) (t % base);
 					}
 					if(valueU[i][j] < 0){
 						found = true;
 						valueU[i][0] = -1;
 						break;
 					}
 					t /= base;
 				}
 				if(found)GvalueU[i] = null;
 				else{
 					for(int j = 0; j < m + 1; j++){
 						GvalueU[i][j] = hashXmore1times(r, valueU[i][j]);
 					}
 				}
 			}
 			long t = U - v;
 			for(int i = 0; i < m + 1; i++){
 				valueU[m][i] = (int) (t % base);
 				t /= base;
 				GvalueU[m][i] = hashXmore1times(r, valueU[m][i]);
 			}
 			for(int i = 0; i < m + 1; i++){
 				GvalueUs[i] = Hasher.computeGeneralHashValue(GvalueU[i]);
 			}
 		}else{
 			for(int i = 0; i < m + 1; i++){
 				for(int j = 0; j < m + 1; j++){
 					valueU[i][j] = 0;
 					GvalueU[i][j] = null;
 				}
 				GvalueUs[i] = null;
 			}
 		}
 	}
 	
 	public void generateL(){
 		GvalueLs = new byte[m + 1][];
 		GvalueL = new byte[m + 1][m + 1][];
 		valueL = new int[m + 1][m + 1];
 		
 		if(whichSide <= 0){
 			for(int i = 0; i < m; i++){
 				long t = v - L;
 				boolean found = false;
 				//long t = x - L - 1; // if '=' is not allowed
 				for(int j = 0; j < m + 1; j++){
 					if(j == 0){
 						valueL[i][j] = (int) (t % base + base);
 					}else if(j <= i){
 						valueL[i][j] = (int) (t % base + base - 1);
 					}else if( j == i + 1){
 						valueL[i][j] = (int) (t % base - 1);
 					}else{
 						valueL[i][j] = (int) (t % base);
 					}
 					if(valueL[i][j] < 0){
 						found = true;
 						valueL[i][0] = -1;
 						break;
 					}
 					t /= base;
 				}
 				if(found)GvalueL[i] = null;
 				else{
 					for(int j = 0; j < m + 1; j ++){
 						GvalueL[i][j] = hashXmore1times(r, valueL[i][j]);
 					}
 				}
 			}
 			long t = v - L;
 			for(int i = 0; i < m + 1; i++){
 				valueL[m][i] = (int) (t % base);
 				t /= base;
 				GvalueL[m][i] = hashXmore1times(r, valueL[m][i]);
 			}
 			for(int i = 0; i < m + 1; i++){
 				GvalueLs[i] = Hasher.computeGeneralHashValue(GvalueL[i]);
 			}
 		}else{
 			for(int i = 0; i < m + 1; i++){
 				for(int j = 0; j < m + 1; j++){
 					valueL[i][j] = 0;
 					GvalueL[i][j] = null;
 				}
 				GvalueLs[i] = null;
 			}
 		}
 		//System.out.println(GvalueL[m][m]);
 	}
 	
 	/**
 	 * @param isL boolean, is this part for L or not
 	 * @param cur int, current id
 	 * @param can int[], composition of current value 
 	 * @return String[] for verification part
 	 * */
 	public byte[] generateVerifyPart(boolean isL, int cur, int[] can){
 		ArrayList<byte[]> str = new ArrayList<byte[]>();
 		buildVO(1, 1, m + 1, cur + 1, str);
 		ByteArrayOutputStream bs = new ByteArrayOutputStream();
 		DataOutputStream ds = new DataOutputStream(bs);
 		DataIO.writeBytes(ds, getDigest());
 		DataIO.writeInt(ds, base);
 		if(isL){
 			DataIO.writeInt(ds, m + 1);
 			DataIO.writeInt(ds, -1);
 			for(int i = 0; i < m + 1; i++){
 				DataIO.writeBytes(ds, hashXmore1times(r, valueL[cur][i] - can[i]));
 			}
 			DataIO.writeBytes(ds, GvalueUs[cur]);
 		}else{
 			DataIO.writeInt(ds, -1);
 			DataIO.writeInt(ds, m + 1);
 			DataIO.writeBytes(ds, GvalueLs[cur]);
 			for(int i = 0; i < m + 1; i++){
 				DataIO.writeBytes(ds, hashXmore1times(r, valueU[cur][i] - can[i]));
 			}
 		}
 		DataIO.writeInt(ds, str.size());
 		for(int i = 0; i < str.size(); i++){
 			DataIO.writeBytes(ds, str.get(i));
 		}
 		return bs.toByteArray();
 	}
 	
 	/**
 	 * Generate VO for server
 	 * 
 	 * @param x
 	 * @param _isL
 	 * 				false means U - x, i.e, x >= v;
 	 * 				true means x - L, i.e., x <= v.
 	 * @return String[]
 	 * @throws Exception
 	 * */
 	public byte[] generateVeryfyPart(long x, boolean _isL){
 		isL = _isL;
 		if(isL && v >= x){
 			x = x - L;
 			int[] can = new int[m + 1];
 			boolean ok = true;
 			for(int i = 0; i < m + 1; i++){
 				can[i] = (int) (x % base);
 				x /= base;
 				if(can[i] > valueL[m][i]){
 					ok = false;
 				}
 			}
 			if(ok){
 				return generateVerifyPart(true, m, can);
 			}
 			for(int i = m - 1; i >= 0; i--){
 				ok = true;
 				for(int j = 0; j < m + 1; j++){
 					if(valueL[i][j] < can[j]){
 						ok = false;
 						break;
 					}
 				}
 				if(ok){
 					return generateVerifyPart(true, i, can);
 				}
 			}
 		}
 		if(!isL && v <= x){
 			x = U - x;
 			int[] can = new int[m + 1];
 			boolean ok = true;
 			for(int i = 0; i < m + 1; i++){
 				can[i] = (int) (x % base);
 				x /= base;
 				if(can[i] > valueU[m][i]){
 					ok = false;
 				}
 			}
 			if(ok){
 				return generateVerifyPart(false, m, can);
 			}
 			for(int i = m - 1; i  >= 0; i--){
 				ok = true;
 				for(int j = 0; j < m + 1; j ++){
 					if(valueU[i][j] < can[j]){
 						ok = false;
 						break;
 					}
 				}
 				if(ok){
 					return generateVerifyPart(false, i, can);
 				}
 			}
 		}
 		System.err.println("G function error in server side!");
 		return null;
 		//throw new Exception("G function error in server side!");
 	}
 	
 	public void buildMHTree(int nd, int l, int r, byte[][] str){
 		if(l == r){
 			MHTree[nd] = str[l - 1];
 			return;
 		}
 		int mid = (l + r) >> 1;
 		buildMHTree(nd * 2, l, mid, str);
 		buildMHTree(nd * 2 + 1, mid + 1, r, str);
 		MHTree[nd] = Hasher.computeGeneralHashValue(new byte[][]{MHTree[nd * 2], MHTree[nd * 2 + 1]});
 	}
 	
 	public void buildVO(int nd, int l, int r, int v, ArrayList<byte[]> str){
 		if(l == r){
 			return;
 		}
 		int mid = (l + r) >> 1;
 		if(v <= mid){
 			str.add(new byte[]{'|'});
 			buildVO(nd * 2, l, mid, v, str);
 			str.add(MHTree[nd * 2 + 1]);
 		}else {
 			str.add(MHTree[nd * 2]);
 			buildVO(nd * 2 + 1, mid + 1, r, v, str);
 			str.add(new byte[]{'|'});
 		}
 	}
 	
 	public static byte[] rebuildVO(int l, int r, byte[][] buf, byte[] str){
 		//System.out.println(l + " " + r);
 		if(l > r){
 			return str;
 		}
		if(buf[l][0] == '|'){
 			return Hasher.computeGeneralHashValue(new byte[][]{rebuildVO(l + 1, r - 1, buf, str), buf[r]});
 		}else{
 			return Hasher.computeGeneralHashValue(new byte[][]{buf[l], rebuildVO(l + 1, r - 1, buf, str)});
 		}
 	}
 	
 	public byte[] getDigest(){
 		if(MHTree != null){
 			return MHTree[1];
 		}
 		byte[][] strs = new byte[m + 1][];
 		for(int i = 0; i <= m; i++){
 			strs[i] = Hasher.computeGeneralHashValue(new byte[][]{GvalueLs[i], GvalueUs[i]});
 		}
 		MHTree = new byte[3 * (m + 1)][];
 		buildMHTree(1, 1, m + 1, strs);
 		return MHTree[1];
 	}
 	
 	public static byte[] clientComputed(DataInputStream ds, long x){
 		int can;
 		byte[] str;
 //		int len = ServerReturned.length;
 		int base = DataIO.readInt(ds);
 		int l = DataIO.readInt(ds);
 		int r = DataIO.readInt(ds);
 		if(l == -1){
 			int m = r - 1;
 			byte[][] strs = new byte[m + 1][];
 			byte[] gValueL = DataIO.readBytes(ds);
 			if(x < 0){
 				try {
 					throw new Exception("value of x < 0!");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			//System.out.println("==========");
 			for(int i = 0; i < m + 1; i++){
 				can = (int) (x % base);
 				x /= base;
 				strs[i] = hashXtimes(DataIO.readBytes(ds), can);
 				//System.out.println(strs[i]);
 			}
 			str = Hasher.computeGeneralHashValue(strs);
 			//System.out.println("Computed : " + str);
 			str = Hasher.computeGeneralHashValue(new byte[][]{gValueL, str});
 			int len = DataIO.readInt(ds);
 			byte[][] serverReturned = new byte[len][];
 			for (int i = 0; i < len; i ++) {
 				serverReturned[i] = DataIO.readBytes(ds);
 			}
 			return rebuildVO(0, len - 1, serverReturned, str);
 		}else if(r == -1){
 			int m = l - 1;
 			byte[][] strs = new byte[m + 1][];
 			if(x < 0){
 				try {
 					throw new Exception("value of x < 0!");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			for(int i = 0; i < m + 1; i++){
 				can = (int)(x % base);
 				x /= base;
 				strs[i] = hashXtimes(DataIO.readBytes(ds), can);
 			}
 			str = Hasher.computeGeneralHashValue(strs);
 			str = Hasher.computeGeneralHashValue(new byte[][]{str, DataIO.readBytes(ds)});
 			int len = DataIO.readInt(ds);
 			byte[][] serverReturned = new byte[len][];
 			for (int i = 0; i < len; i ++) {
 				serverReturned[i] = DataIO.readBytes(ds);
 			}
 			return rebuildVO(0, len - 1, serverReturned, str);
 		}else{
 			try {
 				throw new Exception("Client verified error!");
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	public static boolean verifyValueGreaterThan(byte[] vo, long x) {
 		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(vo));
 		byte[] digest = DataIO.readBytes(ds);
 		return DataIO.compareBytes(digest, clientComputed(ds, x - Gfunction.L));
 	}
 	
 	public static boolean verifyValueLessThan(byte[] vo, long x) {
 		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(vo));
 		byte[] digest = DataIO.readBytes(ds);
 		return DataIO.compareBytes(digest, clientComputed(ds, Gfunction.U - x));
 	}
 
 	@Override
 	public void read(DataInputStream dis) {
 		// TODO Auto-generated method stub
 		try {
 //			L = dis.readLong();
 //			U = dis.readLong();
 			v = dis.readLong();
 			base = dis.readInt();
 			m = (int)(Math.log(U - L) / Math.log(base));
 			int len = dis.readInt();
 			whichSide = dis.readInt();
 			MHTree = new byte[len][];
 			for(int i = 0; i < len ; i++){
 				MHTree[i] = DataIO.readBytes(dis);
 			}
 			valueL = new int[m + 1][m + 1];
 			valueU = new int[m + 1][m + 1];
 			GvalueL = new byte[m + 1][m + 1][];
 			GvalueU = new byte[m + 1][m + 1][];
 			GvalueLs = new byte[m + 1][];
 			GvalueUs = new byte[m + 1][];
 			for(int i = 0; i <= m; i ++){
 				for(int j = 0; j <= m ; j ++){
 					valueL[i][j] = dis.readInt(); // for no using here
 					valueU[i][j] = dis.readInt();
 				}
 				if(dis.readBoolean()){
 					for(int j = 0; j <= m; j++){
 						//System.out.println(i + "\t" + j);
 						GvalueL[i][j] = DataIO.readBytes(dis);
 					}
 				}
 				if(dis.readBoolean()){
 					for(int j = 0; j <= m ;j++){
 						GvalueU[i][j] = DataIO.readBytes(dis);
 					}
 				}
 				GvalueLs[i] = DataIO.readBytes(dis);
 				GvalueUs[i] = DataIO.readBytes(dis);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void write(DataOutputStream dos) {
 		// TODO Auto-generated method stub
 		/*
 		 * 	public long L, U;
 			public String r = "something";
 			public String[][] GvalueU, GvalueL;
 			public int[][] valueU, valueL;
 			public long v;
 			public int base, m;
 		 * */
 		try {
 //			dos.writeLong(L);
 //			dos.writeLong(U);
 			dos.writeLong(v);
 			dos.writeInt(base);
 			dos.writeInt(MHTree.length);
 			//System.out.println("m:\t" + m + " MHTree:\t" + MHTree.length);
 			dos.writeInt(whichSide);
 			for(int i = 0; i < MHTree.length; i++){
 				DataIO.writeBytes(dos, MHTree[i]);
 			}
 			for(int i = 0 ; i <= m; i++){
 				for(int j = 0; j <= m; j++){
 					dos.writeInt(valueL[i][j]);
 					dos.writeInt(valueU[i][j]);
 				}
 				if(GvalueL[i] != null){
 					dos.writeBoolean(true);
 					for(int j = 0; j <= m; j++){
 						DataIO.writeBytes(dos, GvalueL[i][j]);
 					}
 				}else{
 					dos.writeBoolean(false);
 				}
 				if(GvalueU[i] != null){
 					dos.writeBoolean(true);
 					for(int j = 0; j <= m; j++){
 						DataIO.writeBytes(dos, GvalueU[i][j]);
 //						System.out.println(GvalueU[i][j].length);
 					}
 				}else{
 					dos.writeBoolean(false);
 				}
 				DataIO.writeBytes(dos, GvalueLs[i]);
 				DataIO.writeBytes(dos, GvalueUs[i]);
 			}
 			//dos.writeInt(m);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void loadBytes(byte[] data) {
 		// TODO Auto-generated method stub
 		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
 		read(ds);
 	}
 
 
 	@Override
 	public byte[] toBytes() {
 		// TODO Auto-generated method stub
 		ByteArrayOutputStream bs = new ByteArrayOutputStream();
 		DataOutputStream ds = new DataOutputStream(bs);
 		write(ds);
 		return bs.toByteArray();
 	}
 	
 	public byte[] prepareValueGreaterThan(long q) {
 		return generateVeryfyPart(q, true);
 	}
 	
 	public byte[] prepareValueLessThan(long q) {
 		return generateVeryfyPart(q, false);
 	}
 	
 	public static byte[] getDigest(byte[] vo) {
 		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(vo));
 		return DataIO.readBytes(ds);
 	}
 	
 	/**
 	 * It seems if isL = true, x <= val can pass; if isR = true, x >= val can pass.
 	 * 
 	 * 
 	 * */
 	public static void main(String[] args) throws Exception{
 		
 		/**
 		 * test for g function
 		 * */
 		long val = 0, x = 1, times = 1000;
 		Timer timer = new Timer();
 		timer.reset();
 		Gfunction gf = null;
 		for(int i = 0 ; i < times; i ++){
 			gf = new Gfunction(i, 2);
 		}
 		timer.stop();
 		System.out.println("Generator:\t" + timer.timeElapseinMs() / times + "ms");
 		byte[]  vo = null;
 		gf = new Gfunction(val, 2);
 		for(int i = 0 ; i < times; i ++){
 			vo = gf.prepareValueLessThan(x);
 		}
 		timer.stop();
 		System.out.println("SP consume:\t" + timer.timeElapseinMs() / times + "ms");
 		
 		timer.reset();
 		for(int i = 0 ; i < times; i ++){
 			if(Gfunction.verifyValueLessThan(vo, x) == true){
 				//System.out.println("Pass verification!");
 			}else {
 				System.out.println("Fail verification U - x!");
 				break;
 			}
 		}
 		System.out.println("Client consume:\t" + timer.timeElapseinMs() / times + "ms");
		x = -1;
 		gf = new Gfunction(val, 2);
 		vo = gf.prepareValueGreaterThan(x);
 		for(int i = 0 ; i < times; i ++){
 			if(Gfunction.verifyValueGreaterThan(vo, x) == true){
 				//System.out.println("Pass verification!");
 			}else {
 				System.out.println("Fail verification x - L!");
 				break;
 			}
 		}
 		
 		/**
 		 * test file in and file out
 		 * */
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("./database/gf")));
 		for (int i = 0; i < 1; i ++) {
 			gf.write(dos);
 		}
 		dos.flush();
 		dos.close();
 		DataInputStream dis = new DataInputStream(new FileInputStream(new File("./database/gf")));
 		gf.read(dis);
 		dis.close();
 	}
 
 }
