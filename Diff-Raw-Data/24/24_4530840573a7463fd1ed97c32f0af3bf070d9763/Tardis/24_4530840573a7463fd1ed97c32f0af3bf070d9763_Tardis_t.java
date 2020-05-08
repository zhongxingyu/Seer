 package org.aromatic.tardis;
 
 import java.util.*;
 import java.util.regex.*;
 import java.io.*;
 import java.math.*;
 
 class Tardis implements Serializable
 {
     protected static final long serialVersionUID = -1;
     protected static final int V_MAJOR = 0;
     protected static final int V_MINOR = 20;
 
     protected static final String ERRDBID = "invalid DB index";
     protected static final String NOKEY = "no such key";
     protected static final String RANGE = "index out of range";
     protected static final String SYNTAX = "syntax error";
     protected static final String SAMEKEY = "source and destination objects are the same";
     protected static final String WRONGTYPE = "Operation against a key holding the wrong kind of value";
 
     private static final List<String> EMPTYLIST = new ArrayList<String>(0);
     private static final Random random = 
         new Random(System.currentTimeMillis());
     private static final Pattern INTEGERPATTERN = 
         Pattern.compile("^(\\-?\\d+)");
     private static final Pattern FLOATPATTERN = Pattern.compile(
         "[+-]?([0-9]+)(\\.[0-9]+)?([Ee][+-]?[0-9]*)?");
 
     protected static Tardis[] DB = new Tardis[16];
 
     Map<String, Object> repository = new HashMap<String, Object>();
     Map<String, Long> expiry = new HashMap<String, Long>();
 
     public static void save(File f) throws Exception {
         FileOutputStream fos = new FileOutputStream(f);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeUTF("tardis");
         oos.writeInt(V_MAJOR);
         oos.writeInt(V_MINOR);
         oos.writeObject(DB);
         oos.close();
     }
 
     public static void load(File f) throws Exception {
         FileInputStream fis = new FileInputStream(f);
         ObjectInputStream ois = new ObjectInputStream(fis);
 
         String magic = ois.readUTF();
         int major = ois.readInt();
         int minor = ois.readInt();
 
         DB = (Tardis[]) ois.readObject();
         ois.close();
     }
 
     //
     // COMMANDS OPERATING ON STRING VALUES
     //
 
     public synchronized void set(String key, String value)
     {
 	checkExpiry(key, true);
         repository.put(key, (Object)value);
     }
 
     public synchronized String get(String key)
     {
 	checkExpiry(key);
 
         Object v = repository.get(key);
         if (v instanceof String)
             return (String) v;
 
         if (v == null)
             return null;
 
         throw new UnsupportedOperationException(WRONGTYPE);
     }
 
     public synchronized String getset(String key, String value)
     {
 	checkExpiry(key, true);
 
         Object v = repository.get(key);
         if (v instanceof String || v == null)
             return (String) repository.put(key, value);
 
         throw new UnsupportedOperationException(WRONGTYPE);
     }
 
     public synchronized List<String> mget(String keys[])
     {
         List<String> result = new LinkedList<String>();
 
         for (String key : keys) {
 	    checkExpiry(key);
 
             Object v = repository.get(key);
             if (v instanceof String)
                 result.add((String) v);
             else
                 result.add(null);
         }
 
         return result;
     }
 
     public synchronized boolean setnx(String key, Object value)
     {
 	checkExpiry(key, true);
 
         if (repository.containsKey(key))
             return false;
 
         repository.put(key, value);
         return true;
     }
 
     public synchronized int append(String key, String value)
     {
 	checkExpiry(key, true);
 
 	StringBuilder sb = new StringBuilder();
 
         Object v = repository.get(key);
 	if (v instanceof String)
 	    sb.append((String)v);
 	else if (v != null)
             throw new UnsupportedOperationException(WRONGTYPE);
 
 	sb.append(value);
 	repository.put(key, sb.toString());
         return sb.length();
     }
 
     public synchronized void mset(String keyValues[]) {
         for (int i=0; i < keyValues.length-1; i += 2)
 		checkExpiry(keyValues[i], true);
 
         for (int i=0; i < keyValues.length-1; i += 2)
             repository.put(keyValues[i], (Object) keyValues[i+1]);
     }
 
     public synchronized boolean msetnx(String keyValues[]) {
         for (int i=0; i < keyValues.length-1; i += 2)
 		checkExpiry(keyValues[i], true);
 
         for (int i=0; i < keyValues.length-1; i += 2)
             if (repository.get(keyValues[i]) != null) return false;
 
         for (int i=0; i < keyValues.length-1; i += 2)
             repository.put(keyValues[i], (Object) keyValues[i+1]);
         
         return true;
     }
 
     public long incr(String key)
     {
         return incrby(key, 1);
     }
 
     public synchronized long incrby(String key, long n)
     {
         Object value = repository.get(key);
 	long v = 0;
         if (value != null) {
             if (! (value instanceof String))
                 throw new UnsupportedOperationException(WRONGTYPE);
 
             v = getInteger((String)value);
 	}
         v += n;
         repository.put(key, Long.toString(v));
         return v;
     }
 
     public long decr(String key)
     {
         return decrby(key, 1);
     }
 
     public synchronized long decrby(String key, long n)
     {
         Object value = repository.get(key);
 	long v = 0;
         if (value != null) {
             if (! (value instanceof String))
                 throw new UnsupportedOperationException(WRONGTYPE);
 
             v = getInteger((String)value);
         }
         v -= n;
         repository.put(key, Long.toString(v));
         return v;
     }
 
     public synchronized boolean exists(String key)
     {
         return repository.containsKey(key);
     }
 
     public synchronized int del(String keys[])
     {
         int n = 0;
 
         for (String key : keys) {
             if (repository.remove(key) != null) n++;
         }
 
         return n;
     }
 
     public synchronized String type(String key)
     {
         Object v = repository.get(key);
         if (v == null)
             return "none";
         if (v instanceof String)
             return "string";
         if (v instanceof List)
             return "list";
         if (v instanceof Set)
             return "set";
         if (v instanceof ZSet)
             return "zset";
 	if (v instanceof Map)
 	    return "hash";
 
         return "unknown";
     }
 
     //
     // COMMANDS OPERATING ON THE KEY SPACE
     //
     
     public synchronized String keys(String pattern) 
     {
         Set<String> keySet = repository.keySet();
         if (keySet.size() == 0)
             return "";
 
         try {
             StringBuilder result = new StringBuilder();
 
             // convert redis pattern to java regex 
             int l = pattern.length();
 	    for (int i=0; i < l; i++) {
 		char c = pattern.charAt(i);
 		switch (c)
 		{
 		case '\\': // escape next char
 		    result.append(c);
 		    if (i < l-1) {
 		    result.append(pattern.charAt(i+1));
 		    i++;
 		    }
 		    break;
 
 		case '.': // escape .
 		    result.append('\\').append(c);
 		    break;
 
 		case '?':
 		case '*':
 		    result.append('.');
 
 		default:    
 		    result.append(c);
 		}
 	    }
 
             Pattern p = Pattern.compile(result.toString());
             result.setLength(0);
 
             for (String key : keySet) {
             if (p.matcher(key).matches())
                 result.append(key).append(' ');
             }
 
             l = result.length();
             if (l > 0)
                 result.setLength(l-1);
 
             return result.toString();
         } catch(Exception e) {
             return "";
         }
     }
 
     public synchronized String randomkey()
     {
         return getRandom(repository.keySet());
     }
 
     public synchronized void rename(String oldname, String newname)
     {
         if (oldname.equals(newname))
             throw new UnsupportedOperationException(SAMEKEY);
 
         Object v = repository.remove(oldname);
         if (v == null)
             throw new UnsupportedOperationException(NOKEY);
 
         repository.put(newname, v);
     }
 
     public synchronized boolean renamenx(String oldname, String newname)
     {
         if (oldname.equals(newname))
             throw new UnsupportedOperationException(SAMEKEY);
 
         if (!repository.containsKey(oldname))
             throw new UnsupportedOperationException(NOKEY);
 
         if (repository.containsKey(newname))
             return false;    
 
         Object v = repository.remove(oldname);
         repository.put(newname, v);
         return true;
     }
 
     public synchronized boolean expireat(String key, long time) {
 	checkExpiry(key);
 
 	if (expiry.containsKey(key))
 	    return false;
 
 	expiry.put(key, time);
 	return true;
     }
 
     public synchronized long ttl(String key) {
 	checkExpiry(key);
 
 	Long expire = expiry.get(key);
 	if (expire == null)
 	    return -1;
 	else
 	    return expire.longValue() - System.currentTimeMillis();
     }
 
     private boolean checkExpiry(String key) 
     {
 	return checkExpiry(key, false);
     }
 
     private boolean checkExpiry(String key, boolean remove) {
 	Long expire = expiry.get(key);
 	if (expire != null 
 	&& (expire.longValue() <= System.currentTimeMillis() || remove)) {
 System.out.println(key + " expired at " + (expire.longValue()/1000)
 	+ ", now " + (System.currentTimeMillis()/1000));
 	    expiry.remove(key);
 	    repository.remove(key);
 	    return true;
 	} else
 	    return false;
     }
 
     public synchronized int dbsize()
     {
         return repository.size();
     }
 
     //
     // COMMANDS OPERATING ON A LIST
     //
     
     private ArrayList<String> getList(String key, boolean create) {
         Object v = repository.get(key);
         if (v == null) {
             if (create) {
                 v = (Object) new ArrayList<String>();
                 repository.put(key, v);
             }
         } else if (! (v instanceof List))
             throw new UnsupportedOperationException(WRONGTYPE);
 
         return (ArrayList<String>) v;
     }
 
     public synchronized int rpush(String key, String value)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, true);
         list.add(value);
 	return list.size();
     }
     
     public synchronized int lpush(String key, String value)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, true);
         list.add(0, value);
 	return list.size();
     }
     
     public synchronized int llen(String key)
     {
 	checkExpiry(key);
         ArrayList<String> list = getList(key, false);
         return list==null ? 0 : list.size();
     }
 
     public synchronized List<String> lrange(String key, int start, int end)
     {
 	checkExpiry(key);
         ArrayList<String> list = getList(key, false);
         if (list == null)
             return EMPTYLIST;
 
         int size = list.size();
 
         if (start < 0) {
             start += size;
             if (start < 0)
                 start = 0;
         }
 
         if (end < 0) {
             end += size;
             if (end < 0)
                 end = 0;
         }
 
 	if (start > end)
 	    return EMPTYLIST;
 
         if (end >= size)
             end = size-1;
 
         ArrayList<String> result = new ArrayList<String>(list.subList(start, end+1));
         return result;
     }
 
     public synchronized void ltrim(String key, int start, int end)
     {
 	checkExpiry(key);
         ArrayList<String> list = getList(key, false);
         if (list == null)
             return;
 
         int size = list.size();
 
         if (start < 0)
             start += size;
 
         if (end < 0)
             end += size;
         else
             end++;
 
         if (start > end || start >= size) {
             list.clear();
             return;
         }
 
         while (end < size) {
             list.remove(end);
             size--;
         }
 
         for (int i=0; i < start; i++)
             list.remove(0);
     }
 
     public synchronized String lindex(String key, int index)
     {
 	checkExpiry(key);
         ArrayList<String> list = getList(key, false);
         if (list == null)
             return null;
 
         int size = list.size();
 
         if (index < 0)
             index += size;
 
         if (index < 0 || index >= size)
             return null;
 
         return list.get(index);
     }
 
     public synchronized void lset(String key, int index, String value)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, false);
         if (list == null)
             throw new UnsupportedOperationException(NOKEY);
 
         int size = list.size();
 
         if (index < 0)
             index += size;
 
         if (index < 0 || index >= size)
             throw new UnsupportedOperationException(RANGE);
 
         list.set(index, value);
     }
 
     public synchronized int lrem(String key, int count, String value)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, false);
         if (list == null || list.isEmpty())
             return 0;
 
         boolean reverse = count < 0;
         if (count < 0)
             count = -count;
 	else if (count == 0)
 	    count = Integer.MAX_VALUE;
 
         int ret = 0;
 
         while (count > 0) {
             int i;
 
             if (reverse)
                 i = list.lastIndexOf(value);
             else
                 i = list.indexOf(value);
 
             if (i < 0)
                 break;
 
             list.remove(i);
             count--;
             ret++;
         }
 
         return ret;
 
     }
 
     public synchronized String lpop(String key)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, false);
         if (list == null || list.isEmpty())
             return null;
 
         return list.remove(0);
     }
 
     public synchronized String rpop(String key)
     {
 	checkExpiry(key, true);
         ArrayList<String> list = getList(key, false);
         if (list == null || list.isEmpty())
             return null;
 
         return list.remove(list.size()-1);
     }
 
     public synchronized String rpoplpush(String src, String dest)
     {
 	checkExpiry(src, true);
 	checkExpiry(dest, true);
         ArrayList<String> srcList = getList(src, false);
         if (srcList == null || srcList.isEmpty())
             return null;
 
         ArrayList<String> dstList = getList(dest, true);
 
         String v = srcList.remove(srcList.size()-1);
         dstList.add(0, v);
 
         return v;
     }
     
     //
     // COMMANDS OPERATING ON SETS
     //
 
     private Set<String> getSet(String key, boolean create)
     {
         Object v = repository.get(key);
         if (v == null) {
             if (create) {
                 v = (Object) new HashSet<String>();
                 repository.put(key, v);
             }
         } else if (! (v instanceof Set))
             throw new UnsupportedOperationException(WRONGTYPE);
 
         return (Set<String>) v;
     }
 
     public synchronized boolean sadd(String key, String member)
     {
 	checkExpiry(key, true);
         Set<String> set = getSet(key, true);
 
         return set.add(member);
     }
 
     public synchronized boolean srem(String key, String member)
     {
 	checkExpiry(key, true);
         Set<String> set = getSet(key, false);
         if (set == null)
             return false;
 
         return set.remove(member);
     }
 
     public synchronized boolean smove(String src, String dest, String member)
     {
 	checkExpiry(src, true);
 	checkExpiry(dest, true);
         Set<String> srcSet = getSet(src, false);
         if (srcSet == null)
             return false;
 
         Set<String> dstSet = getSet(dest, false); // make sure it's a set
 
         if (srcSet.remove(member) == false)
             return false;
 
         if (dstSet == null)
             dstSet = getSet(dest, true);
 
         dstSet.add(member);
         return true;
     }
 
     public synchronized int scard(String key)
     {
 	checkExpiry(key);
         Set<String> set = getSet(key, false);
         if (set == null)
             return 0;
 
         return set.size();
     }
 
     public synchronized boolean sismember(String key, String member)
     {
 	checkExpiry(key);
         Set<String> set = getSet(key, false);
         if (set == null)
             return false;
 
         return set.contains(member);
     }
 
     public synchronized List<String> sinter(String keys[])
     {
         Set<String> result = null;
 
         for (String key : keys) {
 	    checkExpiry(key);
             Set<String> set = getSet(key, false);
             if (set == null)
                 continue;
             if (result == null)
                 result = new HashSet<String>(set);
             else
                 result.retainAll(set);
         }
 
         if (result != null)
             return new ArrayList<String>(result);
         else
             return EMPTYLIST;
     }
 
     public synchronized int sinterstore(String keys[])
     {
         String resultKey = null;
         Set<String> result = null;
 
         for (String key : keys) {
             if (resultKey == null) {
                 resultKey = key;
 	        checkExpiry(key, true);
                 //getSet(resultKey, false); // check if set
             } else {
 	        checkExpiry(key);
                 Set<String> set = getSet(key, false);
                 if (set == null)
                     continue;
                 if (result == null)
                     result = new HashSet<String>(set);
                 else
                     result.retainAll(set);
             }
         }
 
         if (result == null)
             result = new HashSet<String>();
 
         repository.put(resultKey, result);
         return result.size();
     }
 
     public synchronized List<String> sunion(String keys[])
     {
         Set<String> result = null;
 
         for (String key : keys) {
 	    checkExpiry(key);
             Set<String> set = getSet(key, false);
             if (set == null)
                 continue;
             if (result == null)
                 result = new HashSet<String>(set);
             else
                 result.addAll(set);
         }
 
         if (result != null)
             return new ArrayList<String>(result);
         else
             return EMPTYLIST;
     }
 
     public synchronized int sunionstore(String keys[])
     {
         String resultKey = null;
         Set<String> result = null;
 
         for (String key : keys) {
             if (resultKey == null) {
                 resultKey = key;
 	        checkExpiry(key, true);
                 //getSet(resultKey, false); // check if set
             } else {
 	        checkExpiry(key);
                 Set<String> set = getSet(key, false);
                 if (set == null)
                     continue;
                 if (result == null)
                     result = new HashSet<String>(set);
                 else
                     result.addAll(set);
             }
         }
 
         if (result == null)
             result = new HashSet<String>();
 
         repository.put(resultKey, result);
         return result.size();
     }
 
     public synchronized List<String> sdiff(String keys[])
     {
         Set<String> result = null;
 
         for (String key : keys) {
 	    checkExpiry(key);
             Set<String> set = getSet(key, false);
             if (set == null)
                 continue;
             if (result == null)
                 result = new HashSet<String>(set);
             else
                 result.removeAll(set);
         }
 
         if (result != null)
             return new ArrayList<String>(result);
         else
             return EMPTYLIST;
     }
 
     public synchronized int sdiffstore(String keys[])
     {
         String resultKey = null;
         Set<String> result = null;
 
         for (String key : keys) {
             if (resultKey == null) {
                 resultKey = key;
 	        checkExpiry(key, true);
                 //getSet(resultKey, false); // check if set
             } else {
 	        checkExpiry(key);
                 Set<String> set = getSet(key, false);
                 if (set == null)
                     continue;
                 if (result == null)
                     result = new HashSet<String>(set);
                 else
                     result.removeAll(set);
             }
         }
 
         if (result == null)
             result = new HashSet<String>();
 
         repository.put(resultKey, result);
         return result.size();
     }
 
     public synchronized String spop(String key)
     {
 	checkExpiry(key, true);
         Set<String> set = getSet(key, false);
         if (set == null)
             return null;
 
         String member = getRandom(set);
         set.remove(member);
         return member;
     }
 
     public synchronized String srandmember(String key)
     {
 	checkExpiry(key);
         Set<String> set = getSet(key, false);
         if (set == null)
             return null;
 
         String member = getRandom(set);
         return member;
     }
 
     //
     // COMMANDS OPERATING ON ZSETS
     //
 
     private ZSet getZSet(String key, boolean create)
     {
         Object v = repository.get(key);
         if (v == null) {
             if (create) {
                 v = (Object) new ZSet();
                 repository.put(key, v);
             }
         } else if (! (v instanceof ZSet))
             throw new UnsupportedOperationException(WRONGTYPE);
 
         return (ZSet) v;
     }
 
     public synchronized boolean zadd(String key, String score, String member)
     {
 	checkExpiry(key, true);
         ZSet set = getZSet(key, true);
 
         return set.add(score, member, false)==1.0;
     }
 
     public synchronized double zincrby(String key, String score, String member)
     {
 	checkExpiry(key, true);
         ZSet set = getZSet(key, true);
 
         return set.add(score, member, true);
     }
 
     public synchronized boolean zrem(String key, String member)
     {
 	checkExpiry(key, true);
         ZSet set = getZSet(key, false);
         if (set == null)
             return false;
 
         return set.remove(member);
     }
 
     public synchronized List<String> zrange(String key, int start, int end, boolean reverse, boolean withscores)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return EMPTYLIST;
 
         int size = set.size();
 
         if (start < 0) {
             start += size;
             if (start < 0)
                 start = 0;
         }
 
         if (end < 0) {
             end += size;
             if (end < 0)
                 end = 0;
         }
 
 	if (start > end)
 	    return EMPTYLIST;
 
         if (end >= size)
             end = size-1;
 
 	return set.range(start, end, reverse, withscores);
     }
 
     public synchronized List<String> zrangebyscore(String key, String min, String max, int offset, int end, boolean withscores)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return EMPTYLIST;
 
 	return set.rangebyscore(min, max, offset, end, withscores);
     }
 
     public synchronized int zremrangebyscore(String key, String min, String max)
     {
 	checkExpiry(key, true);
         ZSet set = getZSet(key, false);
         if (set == null)
             return 0;
 
 	return set.remrangebyscore(min, max);
     }
 
     public synchronized int zremrangebyrank(String key, int start, int end)
     {
 	checkExpiry(key, true);
         ZSet set = getZSet(key, false);
         if (set == null)
             return 0;
 
         int size = set.size();
 
         if (start < 0) {
             start += size;
             if (start < 0)
                 start = 0;
         }
 
         if (end < 0) {
             end += size;
             if (end < 0)
                 end = 0;
         }
 
 	if (start > end)
 	    return 0;
 
         if (end >= size)
             end = size-1;
 
 	return set.remrangebyrank(start, end);
     }
 
     public synchronized int zcard(String key)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return 0;
 
         return set.size();
     }
 
     public synchronized String zscore(String key, String member)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return null;
 
         return set.getScore(member);
     }
 
     public synchronized int zrank(String key, String member)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return -1;
 
         return set.getRank(member, true);
     }
 
     public synchronized int zrevrank(String key, String member)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return -1;
 
         return set.getRank(member, false);
     }
 
     public synchronized int zcount(String key, String min, String max)
     {
 	checkExpiry(key);
         ZSet set = getZSet(key, false);
         if (set == null)
             return 0;
 
 	return set.rangebyscore(min, max, 0, Integer.MAX_VALUE, false).size();
     }
 
     //
     // COMMANDS OPERATING ON HASHES
     //
 
     private Map<String, String> getHash(String key, boolean create)
     {
         Object v = repository.get(key);
         if (v == null) {
             if (create) {
                 v = (Object) new HashMap<String, String>();
                 repository.put(key, v);
             }
         } else if (! (v instanceof Map))
             throw new UnsupportedOperationException(WRONGTYPE);
 
         return (Map<String, String>) v;
     }
 
     public synchronized boolean hset(String key, String field, String value)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, true);
 
 	return hash.put(field, value) == null;
     }
 
     public synchronized boolean hsetnx(String key, String field, String value)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, true);
 
 	if (hash.containsKey(field))
 	    return false;
 
 	return hash.put(field, value) == null;
     }
 
     public synchronized long hincrby(String key, String field, long n)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, true);
 	long v = 0;
 
         String value = hash.get(field);
         if (value != null)
             v = getInteger(value);
 	
         v += n;
        hash.put(field, Long.toString(v));
         return v;
     }
 
     public synchronized boolean hdel(String key, String field)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	if (hash == null)
 		return false;
 
 	return hash.remove(field) != null;
     }
 
     public synchronized String hget(String key, String field)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	if (hash == null)
 		return null;
 
 	return hash.get(field);
     }
 
     public synchronized boolean hexists(String key, String field)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	if (hash == null)
 		return false;
 
 	return hash.containsKey(field);
     }
 
     public synchronized int hlen(String key)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	if (hash == null)
 		return 0;
 
 	return hash.size();
     }
 
     public synchronized List<String> hgetall(String key)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	ArrayList<String> result = new ArrayList<String>();
 
 	if (hash == null)
 		return result;
 
 	for (Map.Entry<String, String> e : hash.entrySet()) {
 		result.add(e.getKey());
 		result.add(e.getValue());
 	}
 
 	return result;
     }
 
     public synchronized List<String> hkeys(String key)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	ArrayList<String> result = new ArrayList<String>();
 
 	if (hash == null)
 		return result;
 
 	result.addAll(hash.keySet());
 	return result;
     }
 
     public synchronized List<String> hvals(String key)
     {
 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	ArrayList<String> result = new ArrayList<String>();
 
 	if (hash == null)
 		return result;
 
 	result.addAll(hash.values());
 	return result;
     }
 
    public synchronized List<String> hmget(String keyFields[])
     {
	String key = keyFields[0];

 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	ArrayList<String> result = new ArrayList<String>();
 
 	if (hash == null)
 		return result;
 
	for (int i=1; i < keyFields.length; i++)
		result.add(hash.get(keyFields[i]));
 
 	return result;
     }
 
    public synchronized void hmset(String keyFieldValues[])
     {
	String key = keyFieldValues[0];

 	checkExpiry(key, true);
 	Map<String, String> hash = getHash(key, false);
 
 	ArrayList<String> result = new ArrayList<String>();
 
 	if (hash == null)
 		return;
 
        for (int i=1; i < keyFieldValues.length-1; i += 2)
		hash.put(keyFieldValues[i], keyFieldValues[i+1]);
     }
 
     //
     // SORT
     //
 
     public synchronized List<String> sort(String key, boolean asc, boolean alpha, int start, int count, String pattern_by, List<String> pattern_get, String result)
     {
 	checkExpiry(key);
         Object v = repository.get(key);
         if (v == null)
             throw new UnsupportedOperationException(NOKEY);
 
         if (! (v instanceof Collection) && !(v instanceof ZSet))
             throw new UnsupportedOperationException(WRONGTYPE);
 
 	Collection<String> coll;
 
 	if (v instanceof ZSet)
 	    coll = ((ZSet) v).members();
 	else
 	    coll = (Collection<String>) v;
 
 	if (coll.size() == 0)
 	    return EMPTYLIST;
 
 	if (start > coll.size() || count <= 0)
 	    return EMPTYLIST;
 
 	ArrayList<ScoreObject> list = new ArrayList<ScoreObject>();
 
 	for (String member : coll) {
 	    String sortkey;
 	    if (pattern_by != null) {
 		sortkey = pattern_by.replace("*", member);
 		v = repository.get(sortkey);
 		if (v instanceof String)
 		   sortkey = (String) v;
                 else
 		   sortkey = "";
 	    } else 
 		sortkey = member;
 		    
 	    list.add(new ScoreObject(sortkey, member));
 	}
 
 	int end = start + count;
 	if (end > list.size())
 	    end = list.size();
 
 	Collections.sort(list, new SortComparator(asc, alpha));
 	
 	List<String> sorted = new ArrayList<String>(end-start);
 	for (ScoreObject so : list.subList(start, end)) {
 	    String value = so.value;
 
 	    if (pattern_get.isEmpty())
 	    	sorted.add(value);
 	    else {
 	        for (String pget : pattern_get) {
 		    if (pget.equals("#"))
 		        ; // return value
 		    else {
 		        String getkey = pget.replace("*", value);
 		        v = repository.get(getkey);
 		        if (v instanceof String)
 		           value = (String) v;
 		        else
 		           value = "";
 		    }
 
 	    	    sorted.add(value);
 	        }
 	    }
         }
         
 	if (result != null) {
 		checkExpiry(result, true);
         	repository.put(result, (Object)sorted);
 	}
 
 	return sorted;
     }
 
     //
     // MULTIPLE DATABASE HANDLING COMMANDS
     //
 
     public static Tardis select(int index)
     {
         if (index < 0 || index >= DB.length)
         throw new UnsupportedOperationException(ERRDBID);
 
         synchronized(DB) {
             Tardis db = DB[index];
         if (db == null)
             db = DB[index] = new Tardis();
 
         return db;
         }
     }
 
     public static void flushall()
     {
         for (Tardis t : DB)
             if (t != null) t.flushdb();
     }
 
     public synchronized boolean move(String key, int index)
     {
 
         Object v = repository.get(key);
         if (v == null)
         return false;            // source does not exists
 
         Tardis dst = select(index);
         if (dst.setnx(key, v) == false)
         return false;            // destination already exists
 
         repository.remove(key);
         return true;
     }
 
     public synchronized void flushdb()
     {
         repository.clear();
 	expiry.clear();
     }
 
     ///////////////////////////////////////////////////////////////////
 
     private String getRandom(Set<String> set)
     {
         int size = set.size();
         if (size == 0)
             return null;
 
         int n = random.nextInt(size);
         String value = null;
 
         for (Iterator<String> iter = set.iterator(); iter.hasNext() && n >= 0; n--)
             value = iter.next();
 
         return value;
     }
 
     private static long getInteger(String v)
     {
         Matcher m = INTEGERPATTERN.matcher(v.trim());
         if (m.find()) {
             String n = m.group(0);
             return Long.parseLong(n);
         } else
             return 0;
     }
 
     private static double getDouble(String v)
     {
         Matcher m = FLOATPATTERN.matcher(v.trim());
         if (m.find()) {
             String n = m.group(0);
 	    if (n.length() > 0)
             	return Double.parseDouble(n);
 	    else
                 return 0.0;
         } else if (v.toLowerCase().startsWith("-inf"))
 	    return Double.NEGATIVE_INFINITY;
         else if (v.toLowerCase().startsWith("+inf"))
 	    return Double.POSITIVE_INFINITY;
         else if (v.toLowerCase().startsWith("inf"))
 	    return Double.POSITIVE_INFINITY;
         else if (v.toLowerCase().startsWith("nan"))
 	    return Double.NaN;
 	else
             return 0.0;
     }
 
     private static class ScoreObject implements Serializable
     {
         protected static final long serialVersionUID = -1;
 
 	public String score;
 	public String value;
 
 	ScoreObject(String score, String value) {
 		this.score = score;
 		this.value = value;
 	}
 
 	public String toString() {
 		return "{" + this.score + ":" + this.value + "}";
 	}
     }
 
     private static class SortComparator implements Comparator<ScoreObject>
     {
         boolean asc;
         boolean alpha;
 
         SortComparator(boolean asc, boolean alpha)
         {
             this.asc = asc;
             this.alpha = alpha;
         }
 
         public int compare(ScoreObject o1, ScoreObject o2)
         {
             if (!alpha) {
                 double d1 = getDouble(o1.score);
                 double d2 = getDouble(o2.score);
 
                 if (d1 != d2) {
                     double diff = asc ? d1-d2 : d2-d1;
                     return (diff > 0) ? 1 : -1;
                 }
             }
 
             // if alpha or o1/o2 are not numbers
 
             int result = o1.score.compareTo(o2.score);
             if (asc)
                 return result;
             else
                 return -result;
         }
 
         public boolean equals(Object o)
         {
             return this == (SortComparator) o;
         }
     }
 
     private static class ZComparator implements Comparator<ScoreObject>, Serializable
     {
         protected static final long serialVersionUID = -1;
 
         public int compare(ScoreObject o1, ScoreObject o2)
         {
 	    double d1 = getDouble(o1.score);
 	    double d2 = getDouble(o2.score);
 
 	    // compare by score
 	    if (d1 != d2) {
 		double diff = d1-d2;
 		return (diff > 0) ? 1 : -1;
 	    }
 
 	    // if alpha or o1/o2 are not numbers
 	    int result = o1.score.compareTo(o2.score);
 	    if (result != 0)
 		return result;
 
 	    // compare by value
 	    if (o1.value == null)
 		return o2.value == null ? 0 : -1;
 	    else
 	        return o1.value.compareTo(o2.value);
         }
 
         public boolean equals(Object o)
         {
             return this == (ZComparator) o;
         }
     }
 
     private static class ZSet implements Serializable
     {
         protected static final long serialVersionUID = -1;
 
 	private Map<String, ScoreObject> members;
 	private NavigableSet<ScoreObject> scores;
 
 	public ZSet()
 	{
 	    members = new HashMap<String, ScoreObject>();
 	    scores = new TreeSet<ScoreObject>(new ZComparator());
 	}
 
 	public synchronized double add(String score, String member, boolean incrby)
 	{
 	    ScoreObject so = members.get(member);
 	    if (so != null) {
 		double result = 0.0;
 
 		if (incrby || !so.score.equals(score)) {
 		    scores.remove(so);
 
 		    if (incrby) {
 			result = getDouble(so.score);
 			result += getDouble(score);
 			score = (new BigDecimal(result))
 			    .round(MathContext.DECIMAL64)
 			    .toString();
 		    }
 
 		    so.score = score;
 		    members.put(member, so);
 		    scores.add(so);
 		}
 
 		return result;
 	    }
 
 	    so = new ScoreObject(score, member);
 	    members.put(member, so);
 	    scores.add(so);
 	    return 1.0;
 	}
 
 	public synchronized boolean remove(String member)
 	{
 	    ScoreObject so = members.remove(member);
 	    if (so != null) {
 		scores.remove(so);
 		return true;
 	    }
 
 	    return false;
 	}
 
 	public synchronized List<String> range(int start, int end, boolean reverse, boolean withscores) {
 	    ArrayList<String> list = new ArrayList<String>();
 	    Iterator<ScoreObject> iter = reverse
 		? scores.descendingIterator() : scores.iterator();
 
 	    for (int i=0; i <= end && iter.hasNext(); i++) {
 		ScoreObject so = iter.next();
 		if (i < start)
 		    continue;
 
 		list.add(so.value);
 
 		if (withscores)
 		    list.add(so.score);
 	    }
 
 	    return list;
 	}
 
 	private static String S_MAX = 
 		(new Character(Character.MAX_VALUE)).toString();
 
 	public synchronized List<String> rangebyscore(String min, String max, int offset, int count, boolean withscores) {
 	    ArrayList<String> list = new ArrayList<String>();
 	    if (scores.isEmpty())
 		return list;
 
 	    ScoreObject from;
 	    boolean higher = false;
 
 	    if (min.startsWith("(")) // open
 	        from = scores.higher(new ScoreObject(min.substring(1), S_MAX));
             else
 	        from = scores.ceiling(new ScoreObject(min, ""));
 	    if (from == null)
 		return list;
 
 	    ScoreObject to;
 	    if (max.startsWith("(")) // open
 		to = scores.higher(new ScoreObject(max.substring(1), ""));
 	    else
 		to = scores.higher(new ScoreObject(max + "\0", null));
 
 	    SortedSet<ScoreObject> subset = to != null 
 		? scores.subSet(from, to) : scores.tailSet(from);
 
 	    Iterator<ScoreObject> iter = subset.iterator();
 
 	    for (int i=0; i < offset+count && iter.hasNext(); i++) {
 		ScoreObject so = iter.next();
 		if (i < offset)
 			continue;
 
 		list.add(so.value);
 
 		if (withscores)
 		    list.add(so.score);
 	    }
 
 	    return list;
 	}
 
 	public synchronized int remrangebyscore(String min, String max) {
 	    if (scores.isEmpty())
 		return 0;
 
 	    ScoreObject from = scores.ceiling(new ScoreObject(min, ""));
 	    if (from == null)
 		return 0;
 
 	    ScoreObject to = scores.higher(new ScoreObject(max + "\0", null));
 
 	    SortedSet<ScoreObject> subset = to != null 
 		? scores.subSet(from, to) : scores.tailSet(from);
 
 	    Iterator<ScoreObject> iter = subset.iterator();
 	    int count = 0;
 
 	    while (iter.hasNext()) {
 		ScoreObject so = iter.next();
 		iter.remove();
 		members.remove(so.value);
 		count++;
 	    }
 
 	    return count;
 	}
 
 	public synchronized int remrangebyrank(int start, int end) {
 	    if (scores.isEmpty())
 		return 0;
 
 	    Iterator<ScoreObject> iter = scores.iterator();
 	    int i = 0;
 	    int count = 0;
 
 		// could be optimized a little
 		// if the range is closer to the set's tail
 		// by using a descendingIterator and reverting start&end
 	    while (iter.hasNext()) {
 		ScoreObject so = iter.next();
 
 		if (i >= start) {
 		  iter.remove();
 		  members.remove(so.value);
 		  count++;
 		}
 
 		i++;
 
 		if (i > end)
 		  break;
 	    }
 
 	    return count;
 	}
 
 	public synchronized int size() {
 	    return members.size();
 	}
 
 	public synchronized String getScore(String member) {
 	    ScoreObject so = members.get(member);
 	    return so != null ? so.score : null;
 	}
 
 	public synchronized int getRank(String member, boolean fwd) {
 	    ScoreObject so = members.get(member);
 	    if (so == null)
 	        return -1;
 
 	    if (fwd)
 		    return scores.headSet(so).size();
 	    else
 		    return scores.tailSet(so).size()-1;
 	}
 
 	public synchronized Collection<String> members() {
 	    return members.keySet();
 	}
     }
 }
