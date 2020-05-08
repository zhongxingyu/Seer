 package au.net.netstorm.boost.spider.instantiate;
 
 public interface Nu {
    <T> T nu(Class<? extends T> impl, Object... params);
 }
