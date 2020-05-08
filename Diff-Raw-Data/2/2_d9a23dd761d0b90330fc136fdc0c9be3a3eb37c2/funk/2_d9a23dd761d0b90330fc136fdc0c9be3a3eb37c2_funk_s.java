 package com.svilendobrev.jbase;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.List;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Arrays;
 import junit.framework.Assert;
 import java.util.LinkedHashMap;
 
 public class funk {
 /*
 static public boolean empty( String x) { return x == null || x.length()==0; }
 static public boolean empty( Collection x)     { return x.isEmpty(); }
 static public boolean empty( Map x)    { return x.isEmpty(); }
 static public boolean empty( int x)    { return x == 0; }
 static public boolean empty( long x)   { return x == 0; }
 static public boolean empty( float x)  { return x == 0; }
 use not
 */
 static public boolean not( String x)        { return x == null || x.length()==0; }
 static public boolean not( StringBuilder x) { return x == null || x.length()==0; }
 static public boolean not( Collection x)    { return x == null || x.isEmpty(); }
 static public boolean not( Map x)           { return x == null || x.isEmpty(); }
 static public <T> boolean not( T[] x)       { return x == null || x.length==0; } //references
 static public boolean not( int[] x)         { return x == null || x.length==0; } //primitives
 static public boolean not( long[] x)        { return x == null || x.length==0; }
 static public boolean not( float[] x)       { return x == null || x.length==0; }
 static public boolean not( boolean[] x)     { return x == null || x.length==0; }
 static public boolean not( int x)           { return x == 0; }
 static public boolean not( long x)          { return x == 0; }
 static public boolean not( float x)         { return x == 0; }
 static public boolean not( boolean x)       { return !x; }
 static public boolean not( Date x)          { return x == null; }
 static public boolean not( Boolean x)       { return x == null || !x; }
 static public boolean not( Integer x)       { return x == null || x == 0; }
 static public boolean not( Long    x)       { return x == null || x == 0; }
 static public boolean not( Float   x)       { return x == null || x == 0; }
 static public boolean not( Double  x)       { return x == null || x == 0; }
 
 static public boolean any( String x)        { return x != null && x.length()>0; }
 static public boolean any( StringBuilder x) { return x != null && x.length()>0; }
 static public boolean any( Collection x)    { return x != null && !x.isEmpty(); }
 static public boolean any( Map x)           { return x != null && !x.isEmpty(); }
 static public <T> boolean any( T[] x)       { return x != null && x.length!=0; } //references
 static public boolean any( int[] x)         { return x != null && x.length!=0; } //primitives
 static public boolean any( long[] x)        { return x != null && x.length!=0; }
 static public boolean any( float[] x)       { return x != null && x.length!=0; }
 static public boolean any( boolean[] x)     { return x != null && x.length!=0; }
 static public boolean any( int x)           { return x != 0; }
 static public boolean any( long x)          { return x != 0; }
 static public boolean any( float x)         { return x != 0; }
 static public boolean any( boolean x)       { return x; }
 static public boolean any( Date x)          { return x != null; }
 static public boolean any( Boolean x)       { return x != null && x; }
 static public boolean any( Integer x)       { return x != null && x != 0; }
 static public boolean any( Long    x)       { return x != null && x != 0; }
 static public boolean any( Float   x)       { return x != null && x != 0; }
 static public boolean any( Double  x)       { return x != null && x != 0; }
 
 static public     int len( String x)        { return x.length(); }
 static public     int len( StringBuilder x) { return x.length(); }
 static public     int len( Collection x)    { return x.size(); }
 static public     int len( Map x)           { return x.size(); }
 static public <T> int len( T[] x)           { return x.length; }    //references
 static public     int len( int[] x)         { return x.length; }    //primitives
 static public     int len( long[] x)        { return x.length; }
 static public     int len( boolean[] x)     { return x.length; }
 static public     int len( float[] x)       { return x.length; }
 
 static public <T>  T defaults( T x, T defaults)   { return x != null ? x : defaults; }
 static public String defaults_any( String x, String defaults)   { return any(x) ? x : defaults; }
 
 static public Object first_non_null( Object... values) {
     for (Object o: values)
         if (o != null) return o;
     return null;
 }
 
 
 static public <T>  T get( List<T> x, int i, T defaults) {
     if (not(x)) return defaults;
     if (i<0) i+= len(x);
     if (i<0 || i>= len(x)) return defaults;
     return x.get(i);
 }
 static public <T>  T pop( List<T> x, int i, T defaults) {
     if (not(x)) return defaults;
     if (i<0) i+= len(x);
     if (i<0 || i>= len(x)) return defaults;
     T a = x.get(i);
     x.remove(i);
     return a;
 }
 static public <T>  T get( List<T> x, int i)         { return get( x,i,null); }
 static public <T>  T pop( List<T> x, int i)         { return pop( x,i,null); }
 static public <T>  T pop( List<T> x, T defaults)    { return pop( x,-1,defaults); }
 static public <T>  T pop( List<T> x)                { return pop( x,-1,null); }
 static public <T>  T last( List<T> x, T defaults)   { return get( x,-1,defaults); }
 static public <T>  T last( List<T> x)               { return last( x, null); }
 
 
 static public <T>  T get( T[] x, int i, T defaults) {
     if (not(x)) return defaults;
     if (i<0) i+= len(x);
     if (i<0 || i>= len(x)) return defaults;
     return x[i];
 }
 static public <T>  T get( T[] x, int i)         { return get( x,i,null); }
 static public <T>  T last( T[] x, T defaults)   { return get( x,-1,defaults); }
 static public <T>  T last( T[] x)               { return last( x, null); }
 
 
 static public <K,V>  V setdefault( Map<K,V> m, K key, V vdefault) {
     V v = m.get( key);
     if (v==null) { v = vdefault; m.put( key, v); }
     return v;
 }
 /* doesnt work... Set<String>.class can be faked as Set.class then casted but then newInstance fails..
 static public <K,V>  V setdefault( Map<K,V> m, K key, Class vdef) {
     V v = m.get( key);
     if (v==null) {
         try {
             v = (V)vdef.newInstance();
         } catch (Exception e) { v = null; }
         m.put( key, v);
     }
     return v;
 }
 */
 static public <K,V>  V get( Map<K,V> m, K key, V vdefault) {
     if (not(m)) return vdefault;
     if (!m.containsKey( key)) return vdefault;
     return m.get( key);
 }
 
 
 static public
 void split( String input, String regex, Collection< String> r, boolean skip_empties) {
     for (String g: input.split( regex)) {
         if (skip_empties && not(g)) continue;
         r.add(g);
     }
 }
 static public
 String join( String[] input, String sep, boolean trim ) {
     String r = "";
     for (String g: input) {
         if (any(r)) r+= sep;
         if (trim) g = g.trim();
         r += g;
     }
     if (trim) r = r.trim();
     return r;
 }
 static public
 String join( Collection< String> input, String sep, boolean trim ) {
     String r = "";
     for (String g: input) {
         if (any(r)) r+= sep;
         if (trim) g = g.trim();
         r += g;
     }
     if (trim) r = r.trim();
     return r;
 }
 
 static public
 void split_skip_empties( String input, String regex, Collection< String> r) { split( input, regex, r, true); }
 
 static public
 ArrayList<String> split( String input, String regex, boolean skip_empties ) {
     ArrayList<String> r = new ArrayList();
     split( input, regex, r, skip_empties);
     return r;
 }
 static public
 ArrayList<String> split_skip_empties( String input, String regex ) {
     return split( input, regex, true);
 }
 
 static public   ArrayList<String> split( String input) { return split_skip_empties( input, " "); }
 static public   String[]          split2array( String input ) { return toArray( split( input)); }
 
 static public   String rsplit_last( String input, String regex ) {
     ArrayList<String> r = split_skip_empties( input, regex);
     if (funk.not( r)) return null;
     return r.get( r.size() -1 );
 }
 
 static public   String join_trim( Collection< String> input, String sep) { return join( input, sep, true); }
 static public   String join_trim( String[] input, String sep) { return join( input, sep, true); }
 static public   String join( Collection< String> input, String sep) { return join( input, sep, false); }
 static public   String join( String[] input, String sep) { return join( input, sep, false); }
 
 static public
 void trim( List< String> r) {
     for (int i=0; i<r.size(); i++)
         r.set( i, r.get(i).trim());
 }
 
 static public
 String[] toArray( Collection< String> x) { return (String[]) x.toArray( new String[0] ); }
 
 static public
 <T> void addAll( Collection<T> to, T[] from) { if (from != null) for (T x:from) to.add( x); }
 
 /*
 static public <T> T[] toArray( Collection<T> x) { return (T[]) x.toArray( new T[0] ); }
 */
 
 static public
 boolean in_split( String needle, String haystack) { return split( haystack).contains( needle); }
 
 static public
 <T> int indexOf( List<T> c, T x) { return (null==c) ? -1 : c.indexOf( x); }
 static public
 <T> int indexOf( T[] c,     T x) {
     if (null==c) return -1;
     for (int i=0; i<c.length; i++)
         if (c[i].equals(x)) return i;
     return -1;
 }
 
 
 static public
 void printStack( String pfx) {
     Log.d( pfx);
     try {
         throw new Exception();
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
 
 static public int cmp( String a, String b)  { return a.compareTo( b); }
 static public int cmp( Date a, Date b)      { return a.compareTo( b); }
 static public int cmp( int a,   int b)      { return a<b?-1:(a>b?+1:0); }
 static public int cmp( long a,  long b)     { return a<b?-1:(a>b?+1:0); }
 static public int cmp( float a, float b)    { return a<b?-1:(a>b?+1:0); }
 static public int cmp( boolean a, boolean b){ return a==b ? 0 : (a?+1:-1); }
 
 static public final int NONULL = 22;
 static public int _cmpnull( Object a, Object b, boolean nulls_first) {
     //null's first
     if (a==null && b==null) return 0;
     if (a==null) return nulls_first ? -1 : +1;
     if (b==null) return nulls_first ? +1 : -1;
     return NONULL;
 }
 static public int cmpnull( String a, String b,  boolean nulls_first)             { int r= _cmpnull( a,b, nulls_first); return r!=NONULL ? r : cmp(a,b); }
 static public int cmpnull( Date a,   Date b,    boolean nulls_first)             { int r= _cmpnull( a,b, nulls_first); return r!=NONULL ? r : cmp(a,b); }
 static public int cmpnull_ignorecase( String a, String b, boolean nulls_first)   { int r= _cmpnull( a,b, nulls_first); return r!=NONULL ? r : a.compareToIgnoreCase(b); }
 static public int cmpnull_ignorecase_str( Object a, Object b, boolean nulls_first) { int r= _cmpnull( a,b, nulls_first); return r!=NONULL ? r : ((String)a).compareToIgnoreCase((String)b); }
 
 static public int cmpnull( String a, String b)  { return cmpnull(a,b,true); }
 static public int cmpnull( Date a,   Date b)    { return cmpnull(a,b,true); }
static public int cmpnull_ignorecase( String a, String b)  { return cmpnull(a,b,true); }
 
 static public boolean eq( String a, String b) { return a==null && b==null || a!=null && b!=null && a.equals(b); }
 static public boolean eq_ignorecase( String a, String b) { return a==null && b==null || a!=null && b!=null && a.equalsIgnoreCase(b); }
 static public boolean ne( String a, String b) { return !eq(a,b); }
 static public boolean lt( String a, String b) { return a.compareTo(b) < 0; }
 static public boolean gt( String a, String b) { return a.compareTo(b) > 0; }
 static public boolean le( String a, String b) { return a.compareTo(b) <=0; }
 static public boolean ge( String a, String b) { return a.compareTo(b) >=0; }
 
 static public boolean eq( Date a, Date b)   { return a.equals(b); }
 static public boolean ne( Date a, Date b)   { return !eq(a,b); }
 static public boolean lt( Date a, Date b)   { return a.before(b); }
 static public boolean gt( Date a, Date b)   { return a.after(b); }
 static public boolean le( Date a, Date b)   { return !a.after(b); }
 static public boolean ge( Date a, Date b)   { return !a.before(b); }
 
 static public boolean eq( Calendar a, Calendar b)   { return a==null && b==null || a!=null && b!=null && eq( a.getTime(), b.getTime()); }
 static public boolean ne( Calendar a, Calendar b)   { return !eq(a,b); }
 static public boolean lt( Calendar a, Calendar b)   { return lt( a.getTime(), b.getTime()); }
 static public boolean gt( Calendar a, Calendar b)   { return gt( a.getTime(), b.getTime()); }
 static public boolean le( Calendar a, Calendar b)   { return le( a.getTime(), b.getTime()); }
 static public boolean ge( Calendar a, Calendar b)   { return ge( a.getTime(), b.getTime()); }
 
 static public boolean overlap_nonzero(      Date as,Date ae, Date bs,Date be) { return gt( be, as) && lt( bs, ae); }
 static public boolean overlap_or_neighbour( Date as,Date ae, Date bs,Date be) { return ge( be, as) && le( bs, ae); }
 
 
 //static public <T> boolean eq( T a, T b)   { return cmp(a,b); }
 
 static public String min( String a, String b)   { return lt(a,b) ? a : b; }
 static public Date   min( Date   a, Date   b)   { return lt(a,b) ? a : b; }
 static public int    min( int    a, int    b)   { return a<b ? a : b; }
 static public long   min( long   a, long   b)   { return a<b ? a : b; }
 static public float  min( float  a, float  b)   { return a<b ? a : b; }
 static public boolean min( boolean a, boolean b){ return a && b; }
 
 static public String max( String a, String b)   { return gt(a,b) ? a : b; }
 static public Date   max( Date   a, Date   b)   { return gt(a,b) ? a : b; }
 static public int    max( int    a, int    b)   { return a>b ? a : b; }
 static public long   max( long   a, long   b)   { return a>b ? a : b; }
 static public float  max( float  a, float  b)   { return a>b ? a : b; }
 static public boolean max( boolean a, boolean b){ return a || b; }
 
 
 static public long diff_ms( Date a, Date b)  { return a.getTime() - b.getTime(); }
 static public int  diff_s( Date a, Date b)   { return (int)((a.getTime() - b.getTime())/1000); }
 
 
 static public
 boolean same( Class[][] x, Class[][] y) {
     //if (x == null && y == null) return true;
     //if (x == null || y == null) return false;
     int n = funk.len(x);
     if (n != funk.len(y)) return false;
     for (; --n>=0; ) {
         int m = funk.len( x[n] );
         if (m != funk.len( y[n])) return false;
         for ( ; --m>=0; )
             if (!x[n][m].equals( y[n][m])) return false;
     }
     return true;
 }
 
 static public boolean contains( String c, String x)     { return c != null && x != null && c.contains( x); }
 static public boolean contains_aslowercase( String c, String x) {
     return c != null && x != null && c.toLowerCase().contains( x.toLowerCase()); }
 static public boolean contains( Collection c, Object x) { return c != null && c.contains( x); }
 static public boolean contains( Map c, Object x)        { return c != null && c.containsKey( x); }
 static public boolean containsValue( Map c, Object x)   { return c != null && c.containsValue( x); }
 //static public <T> boolean contains( T[] c, T x) {
 static public boolean contains( int[] c, int x) {
     if (c!=null)
         for (int z: c)
             if (z == x) return true;
     return false;
 }
 
 static public
 <T> void addAll( Collection<T> dest, Collection< ? extends T> src) {
     if (any(src)) dest.addAll(src);
 }
 /*
 static public
 <T> void addAll( Collection<T> dest, Collection< T> src) {
     if (any(src)) dest.addAll(src);
 }
 */
 
 static public
 <T,S> boolean eq( Collection< T> a, Collection< S> b) { return a==null && b==null || a!=null && b!=null && a.equals(b); }
 
 
 static public
 void assertTrue( boolean expr) { Assert.assertTrue( expr); }
 static public
 void fail( String txt) { Assert.fail( txt); }
 static public
 void assertNotnull( Object x) { Assert.assertTrue( x != null); }
 
 
 static public
 void print( int[] xx, String pfx) {
     String r = "";
     for (int x: xx) {
         if (any(r)) r+= ",";
         r += x;
     }
     Log.d( pfx+" " + r);
 }
 
 static public
 String escapeXML( String s) {
     StringBuffer sb = new StringBuffer();
     int n = s.length();
     for (int i = 0; i < n; i++) {
         char c = s.charAt(i);
         switch (c) {
             case '<': sb.append("&lt;"); break;
             case '>': sb.append("&gt;"); break;
             case '&': sb.append("&amp;"); break;
             case '"': sb.append("&quot;"); break;
             case '\'': sb.append("&#039;"); break;
             default:  sb.append(c); break;
         }
     }
     return sb.toString();
 }
 
 static public
 String escape( String text, String esc, String[] delims) {
     text = text.replace( esc, esc + esc);
     for (String s : delims)
         text = text.replace( s, esc + s);
     return text;
 }
 static public
 String unescape( String text, String esc, String[] delims) {
     for (String s : delims)
         text = text.replace( esc + s, s);
     text = text.replace( esc + esc, esc);
     return text;
 }
 
 static public
 String capitalize( String x) {
     if (not(x)) return x;
     return x.substring( 0,1).toUpperCase() + x.substring( 1);
 }
 
 /*
 static public <K,V> LinkedHashMap<K,V> cacheMap( final int max_capacity, int initial_capacity) {
     return new LinkedHashMap< K, V>( initial_capacity, (float)0.75, true) {
             protected boolean removeEldestEntry( Map.Entry eldest) { return size() > max_capacity; }
         };
 }
 */
 
 static public class CacheMap <K,V> extends LinkedHashMap<K,V> {
     int max_capacity;
     public CacheMap( int max_capacity, int initial_capacity) {
         super( initial_capacity, (float)0.75, true);
         this.max_capacity = max_capacity;
     }
     public CacheMap( int max_capacity) { this( max_capacity, max_capacity); }
 
     protected boolean removeEldestEntry( Map.Entry eldest) { return size() > max_capacity; }
 }
 
 } //funk
 // vim: ts=4:sw=4:expandtab
