 package dk.client;
 
 public class IntegerMath {
 	public static native void exportStaticMethod() /*-{
 		$wnd.mul = $entry(@dk.client.IntegerMath::mul(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.div = $entry(@dk.client.IntegerMath::div(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.mod = $entry(@dk.client.IntegerMath::mod(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.sub = $entry(@dk.client.IntegerMath::sub(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.add = $entry(@dk.client.IntegerMath::add(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.or  = $entry(@dk.client.IntegerMath::or(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.and = $entry(@dk.client.IntegerMath::and(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.xor = $entry(@dk.client.IntegerMath::xor(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.shl = $entry(@dk.client.IntegerMath::shl(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.ashr= $entry(@dk.client.IntegerMath::ashr(Ljava/lang/Long;Ljava/lang/Long;));
 		$wnd.lshr= $entry(@dk.client.IntegerMath::lshr(Ljava/lang/Long;Ljava/lang/Long;));
 
 		$wnd.mulss = $entry(@dk.client.IntegerMath::mulss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.divss = $entry(@dk.client.IntegerMath::divss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.modss = $entry(@dk.client.IntegerMath::modss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.subss = $entry(@dk.client.IntegerMath::subss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.addss = $entry(@dk.client.IntegerMath::addss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.orss = $entry(@dk.client.IntegerMath::orss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.andss = $entry(@dk.client.IntegerMath::andss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.xorss = $entry(@dk.client.IntegerMath::xorss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.shlss = $entry(@dk.client.IntegerMath::shlss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.ashrss = $entry(@dk.client.IntegerMath::ashrss(Ljava/lang/String;Ljava/lang/String;));
 		$wnd.lshrss = $entry(@dk.client.IntegerMath::lshrss(Ljava/lang/String;Ljava/lang/String;));
 	}-*/;
 
 /*
 	public static String mul(long d1, long q) {
 		int e = (int) ((d1*q) & 0xffffffff);
 		return Integer.toHexString(e) + " : " + String.valueOf(e);
 	}
 */
 	public static int mul(Long d1, Long q) {
 		return (int) ((d1*q) & 0xffffffff);
 	}
 	public static int div(Long d1, Long q) {
		return (int) ((d1/q) & 0xffffffff);
 	}
 	public static int mod(Long d1, Long q) {
 		return (int) ((d1%q) & 0xffffffff);
 	}
 	public static int sub(Long d1, Long q) {
 		return (int) ((d1-q) & 0xffffffff);
 	}
 	public static int add(Long d1, Long q) {
 		return (int) ((d1+q) & 0xffffffff);
 	}
 	public static int or(Long d1, Long q) {
 		return (int) ((d1|q) & 0xffffffff);
 	}
 	public static int and(Long d1, Long q) {
 		return (int) ((d1&q) & 0xffffffff);
 	}
 	public static int xor(Long d1, Long q) {
 		return (int) ((d1^q) & 0xffffffff);
 	}
 	public static int shl(Long d1, Long q) {
 		return (int) ((d1<<q) & 0xffffffff);
 	}
 	public static int ashr(Long d1, Long q) {
 		return (int) ((d1>>q) & 0xffffffff);
 	}
 	public static int lshr(Long d1, Long q) {
 		return (int) ((d1>>>q) & 0xffffffff);
 	}
 	public static String mulss(String a, String b) {
 		return String.valueOf(mul(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String addss(String a, String b) {
 		return String.valueOf(add(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String divss(String a, String b) {
 		return String.valueOf(div(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String modss(String a, String b) {
 		return String.valueOf(mod(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String subss(String a, String b) {
 		return String.valueOf(sub(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String orss(String a, String b) {
 		return String.valueOf(or(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String andss(String a, String b) {
 		return String.valueOf(and(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String xorss(String a, String b) {
 		return String.valueOf(xor(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String shlss(String a, String b) {
 		return String.valueOf(shl(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String ashrss(String a, String b) {
 		return String.valueOf(ashr(Long.valueOf(a),Long.valueOf(b)));
 	}
 	public static String lshrss(String a, String b) {
 		return String.valueOf(lshr(Long.valueOf(a),Long.valueOf(b)));
 	}
 
 //		long d1 = 0x847c9b5d;
 //		long q =  0x549530e1;
 }
