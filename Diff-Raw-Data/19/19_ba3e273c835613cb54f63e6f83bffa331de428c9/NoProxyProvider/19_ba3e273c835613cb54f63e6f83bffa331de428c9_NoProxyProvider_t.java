 package ru.chuprikov.search.gather.fetcher;
 
 import java.net.Proxy;
 
 public class NoProxyProvider implements ProxyProvider {
     @Override
     public Proxy getProxy() {
         return Proxy.NO_PROXY;
     }
 }
