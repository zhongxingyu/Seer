 package au.net.netstorm.boost.spider.instantiate;
 
 public interface Nu {
    <T, U extends T> T nu(Class<U> impl, Object... params);
 }
