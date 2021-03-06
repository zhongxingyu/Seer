 package ru.mail.teamcity.avatar.service;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * User: Grigory Chernyshev
  * Date: 12.05.13 23:31
  */
class AvatarCache {
  private static final ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();
 
  @Nullable
   public static String getCache(@NotNull String username) {
     return cache.get(username);
   }
 
   public static void setCache(@NotNull String username, @Nullable String url) {
     AvatarCache.cache.put(username, url);
   }
 }
