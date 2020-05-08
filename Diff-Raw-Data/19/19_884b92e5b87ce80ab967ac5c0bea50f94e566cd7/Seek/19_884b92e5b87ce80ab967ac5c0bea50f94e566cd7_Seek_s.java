 package redis.seek;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.pool.impl.GenericObjectPool.Config;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisShardInfo;
 import redis.clients.jedis.Pipeline;
 import redis.clients.jedis.ShardedJedis;
 import redis.clients.jedis.ShardedJedisPool;
 import redis.clients.util.Hashing;
 import redis.clients.util.SafeEncoder;
 
 public class Seek {
     public static final String TOTAL = "t";
     public static final String TAGS = "tg";
     public static final String INFO = "i";
     public static final String FIELDS = "f";
     public static final String QUERIES = "q";
     public static final String QUERIES_TMP = "t";
     public static final String QUERIES_RESULT = "r";
 
     private static ShardedJedisPool pool;
 
     public static void configure(Config config, List<JedisShardInfo> shards) {
         pool = new ShardedJedisPool(config, shards, Hashing.MURMUR_HASH);
     }
 
     public static ShardedJedisPool getPool() {
         return pool;
     }
 
     public Search search() {
         return new Search();
     }
 
     public Search search(String... shards) {
         return new Search(shards);
     }
 
     @SuppressWarnings("unchecked")
     public Info<String, Info<String, Long>> info(String... shards) {
         Info<String, Info<String, Long>> info = new Info<String, Info<String, Long>>();
         Nest base = new Nest("");
         if (shards != null) {
             for (String shard : shards) {
                 base.cat(shard);
             }
         }
         final Nest idx = base.fork();
         ShardedJedis jedis = getPool().getResource();
         Jedis shard = jedis.getShard(idx.key());
         String stotal = shard.get(idx.cat(Seek.INFO).cat(Seek.TOTAL).key());
         try {
             info.setTotal(Integer.parseInt(stotal));
         } catch (NumberFormatException e) {
             info.setTotal(0);
         }
         final Map<String, String> facets = shard.hgetAll(idx.cat(Seek.INFO)
                 .key());
         Pipeline p = shard.pipelined();
         for (String facetField : facets.keySet()) {
             p.hgetAll(idx.cat(Seek.INFO).cat(facetField).key());
         }
         List<Object> data = p.execute();
         getPool().returnResource(jedis);
         Iterator<Object> iterator = data.iterator();
         for (String facetField : facets.keySet()) {
             List<Object> next = (List<Object>) iterator.next();
             Iterator<Object> it = next.iterator();
             Info<String, Long> m = new Info<String, Long>();
             m.setTotal(Long.parseLong(facets.get(facetField)));
             while (it.hasNext()) {
                 m.put(SafeEncoder.encode((byte[]) it.next()), Long
                         .parseLong(SafeEncoder.encode((byte[]) it.next())));
             }
            info.put(facetField, m);
         }
         return info;
     }
 
     public void clearInfo(String... shards) {
         Nest base = new Nest("");
         if (shards != null) {
             for (String shard : shards) {
                 base.cat(shard);
             }
         }
         final Nest idx = base.fork();
         ShardedJedis jedis = getPool().getResource();
         final Jedis shard = jedis.getShard(idx.key());
         shard.del(idx.cat(Seek.INFO).cat(Seek.TOTAL).key());
         final Map<String, String> facets = shard.hgetAll(idx.cat(Seek.INFO)
                 .key());
         Pipeline p = shard.pipelined();
         for (String facetField : facets.keySet()) {
             p.del(idx.cat(Seek.INFO).cat(facetField).key());
         }
         p.del(idx.cat(Seek.INFO).key());
         p.execute();
         getPool().returnResource(jedis);
     }
 
     public Entry add(String id, Double order) {
         return new Entry(this, id, order);
     }
 
     @SuppressWarnings("unchecked")
     public void remove(String id, String... shards) {
         Nest idx = new Nest("");
         for (String field : shards) {
             idx.cat(field);
         }
         Nest ndx = idx.fork();
         idx = ndx.cat(id).fork();
         ShardedJedis jedis = Seek.getPool().getResource();
         Jedis shard = jedis.getShard(ndx.key());
         try {
             Pipeline p = shard.pipelined();
             // List<String> indexes =
             p.lrange(idx.key(), 0, -1);
             // Map<String, String> fields =
             p.hgetAll(idx.cat(Seek.FIELDS).key());
             // List<String> tags =
             p.lrange(idx.cat(Seek.TAGS).key(), 0, -1);
             List<Object> data = p.execute();
             List<byte[]> indexes = (List<byte[]>) data.get(0);
             List<byte[]> fields = (List<byte[]>) data.get(1);
             List<byte[]> tags = (List<byte[]>) data.get(2);
 
             p = shard.pipelined();
             p.decr(ndx.cat(Seek.INFO).cat(Seek.TOTAL).key());
             for (byte[] index : indexes) {
                 p.zrem(SafeEncoder.encode(index), id);
             }
             p.del(idx.key());
             for (byte[] tag : tags) {
                 p.hincrBy(ndx.cat(Seek.INFO).cat(Seek.TAGS).key(), SafeEncoder
                         .encode(tag), -1);
             }
             Iterator<byte[]> iterator = fields.iterator();
             while (iterator.hasNext()) {
                 String field = SafeEncoder.encode(iterator.next());
                 String key = SafeEncoder.encode(iterator.next());
                 p.hincrBy(ndx.cat(Seek.INFO).key(), field, -1);
                 p.hincrBy(ndx.cat(Seek.INFO).cat(field).key(), key, -1);
             }
             p.del(idx.cat(Seek.FIELDS).key());
             p.del(idx.cat(Seek.TAGS).key());
             p.execute();
             Seek.getPool().returnResource(jedis);
         } catch (Exception e) {
             Seek.getPool().returnBrokenResource(jedis);
             throw new SeekException(e.getMessage());
         }
     }
 }
