 package com.lucastheisen.autotagger.tag;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class GenericsTest {
     class Box {
         private String data;
 
         public String getData() {
             return data;
         }
     }
 
     class BoxA extends Box {
         private String adata;
         
         BoxA( String adata ) {
             this.adata = adata;
         }
 
         public String getAData() {
             return adata;
         }
     }
 
     class BoxB extends Box {
         private String bdata;
         
         BoxB( String bdata ) {
             this.bdata = bdata;
         }
 
         public String getBData() {
             return bdata;
         }
     }
 
     abstract class Parser<T> {
         abstract void parse( T box );
     }
 
     class ParserA extends Parser<BoxA> {
         @Override
         public void parse( BoxA box ) {
             System.out.print( "BoxA: " + box.getAData() );
         }
     }
 
     class ParserB extends Parser<BoxB> {
         @Override
         public void parse( BoxB box ) {
             System.out.print( "BoxB: " + box.getBData() );
         }
     }
 
     class Registry {
         Map<Class<?>, Parser<?>> unsafeMap = new HashMap<>();
 
         <T extends Box, S extends Parser<T>> void add( Class<T> clazz, S parser ) {
             unsafeMap.put( clazz, parser );
         }
 
         <T extends Box> boolean containsKey( Class<T> clazz ) {
             return unsafeMap.containsKey( clazz );
         }
 
         @SuppressWarnings( "unchecked" )
         <T extends Box, S extends Parser<T>> S get( Class<T> clazz ) {
             return (S) unsafeMap.get( clazz );
         }
     }
     
    private <T extends Box> void parse( Registry registry, Class<T> clazz, Box  box ) {
        registry.get( clazz ).parse( clazz.cast( box ) );        
    }
    
     public void runTest() {
         Registry registry = new Registry();
         registry.add( BoxA.class, new ParserA() );
         registry.add( BoxB.class, new ParserB() );
         
         List<Box> boxes = new ArrayList<>();
         boxes.add( new BoxA( "Silly" ) );
         boxes.add( new BoxB( "Funny" ) );
         boxes.add( new BoxB( "Foo" ) );
         boxes.add( new BoxA( "Bar" ) );
         
         for ( Box box : boxes ) {
             Class<? extends Box> clazz = box.getClass();
            parse( registry, clazz, box );
         }
     }
 
     public static void main( String[] args ) {
         new GenericsTest().runTest();
     }
 }
