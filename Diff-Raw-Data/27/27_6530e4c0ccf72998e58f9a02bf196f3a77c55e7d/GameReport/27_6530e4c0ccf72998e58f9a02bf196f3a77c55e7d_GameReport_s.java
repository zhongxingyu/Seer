 package org.drooms.api;
 
 import java.io.OutputStream;
 import java.util.Map;
 
 public interface GameReport<S extends Situation<P, N, E>, P extends Playground<N, E>, N extends Node, E extends Edge<N>> {
 
     public void addTurn(S situation, Map<Player, Integer> points);
     
    public void write(OutputStream s);
 
 }
