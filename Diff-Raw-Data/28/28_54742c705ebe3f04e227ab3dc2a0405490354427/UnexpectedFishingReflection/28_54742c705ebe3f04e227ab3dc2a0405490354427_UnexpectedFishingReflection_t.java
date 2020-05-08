 /*
  *  Copyright 2013 Ivan Molodetskikh.
  *  
  *  This file is part of UnexpectedFishing.
  *
  *  UnexpectedFishing is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  UnexpectedFishing is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with UnexpectedFishing.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unexpectedfishing;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import org.bukkit.Bukkit;
 
 public class UnexpectedFishingReflection
 {
     public static UnexpectedFishingReflectionCache entityFallingBlock;
     public static UnexpectedFishingReflectionCache craftFallingSand;
     
     public static UnexpectedFishingReflectionCache forgeEntityFallingSand;
     
     public static boolean reflectEntityFallingBlock()
     {
         entityFallingBlock = new UnexpectedFishingReflectionCache();
         
         Class entityFallingBlockClass = getClass( getEntityFallingBlockClassName() );
         if ( entityFallingBlockClass != null )
         {
             entityFallingBlock.available = true;
             
             Field hurtEntities = getField( entityFallingBlockClass, "hurtEntities" );
             if ( hurtEntities != null )
             {
                 entityFallingBlock.storeField( "hurtEntities", hurtEntities );
                 
                 entityFallingBlock.compatible = true;
             }
         }
         
         if ( !entityFallingBlock.compatible )
         {
             UnexpectedFishingMain.log( "Could not reflect EntityFallingBlock! Anvils are disabled." );
         }
         
         return entityFallingBlock.compatible;
     }
     
     public static boolean reflectCraftFallingSand()
     {
         craftFallingSand = new UnexpectedFishingReflectionCache();
         
         Class craftFallingSandClass = getClass( "org.bukkit.craftbukkit." + getBukkitVersionString() + ".entity.CraftFallingSand");
         if ( craftFallingSandClass != null )
         {
             craftFallingSand.available = true;
             
             Method getHandle = getMethod( craftFallingSandClass, "getHandle", new Class[0] );
             if ( getHandle != null )
             {
                 craftFallingSand.storeMethod( "getHandle", getHandle );
                 
                 craftFallingSand.compatible = true;
             }
         }
         
         if ( !craftFallingSand.compatible )
         {
             UnexpectedFishingMain.log( "Could not reflect CraftFallingSand! Anvils are disabled." );
         }
         
         return craftFallingSand.compatible;
     }
     
     public static boolean reflectForgeEntityFallingSand()
     {
         forgeEntityFallingSand = new UnexpectedFishingReflectionCache();
         
         Class forgeEntityFallingSandClass = getClass( "net.minecraft.entity.item.EntityFallingSand" );
         if ( forgeEntityFallingSandClass != null )
         {
             forgeEntityFallingSand.available = true;
             
             Field isAnvil = getField( forgeEntityFallingSandClass, "field_82155_f" );
             if ( isAnvil != null )
             {
                 forgeEntityFallingSand.storeField( "isAnvil", isAnvil );
                 
                 forgeEntityFallingSand.compatible = true;
             }
         }
         
         if ( !forgeEntityFallingSand.compatible )
         {
             UnexpectedFishingMain.log( "Could not reflect Forge's EntityFallingSand! Anvils are disabled." );
         }
         
         return forgeEntityFallingSand.compatible;
     }
     
     public static String getEntityFallingBlockClassName()
     {
         return "net.minecraft.server." + getBukkitVersionString() + ".EntityFallingBlock";
     }
     
     public static String getBukkitVersionString()
     {
         String packageName = Bukkit.getServer().getClass().getPackage().getName();
         return packageName.substring(packageName.lastIndexOf('.') + 1);
     }
     
     public static boolean is( Object object, String name )
     {
         return object.getClass().getSimpleName() == name;
     }
     
     public static boolean doesClassExist( String name )
     {
         Class clazz = getClass( name );
         return clazz != null;
     }
     
     public static Class getClass( String name )
     {
         try
         {
             return Class.forName( name );
         }
         catch ( ClassNotFoundException e )
         {
             ;
         }
         
         return null;
     }
     
     public static Field getField( Class clazz, String name )
     {
         try
         {
             Field field = null;
             
             try
             {
                 field = clazz.getField( name );
             }
             catch ( Exception e )
             {
                 field = null;
             }
             
             if ( field == null )
             {
                 field = clazz.getDeclaredField( name );
             }
             
             field.setAccessible( true );
             return field;
         }
         catch ( Exception e )
         {
             if ( name != "ofProfiler" )
             {
                 UnexpectedFishingMain.log( "Could not retrieve field \"" + name + "\" from class \"" + clazz.getName() + "\"" );
                 e.printStackTrace();
             }
         }
         
         return null;
     }
     
     public static Field getFinalField( Class clazz, String name )
     {
         try
         {
             Field field = null;
             
             try
             {
                 field = clazz.getField( name );
             }
             catch ( Exception e )
             {
                 field = null;
             }
             
             if ( field == null )
             {
                 field = clazz.getDeclaredField( name );
             }
             
             Field modifiers = Field.class.getDeclaredField( "modifiers" );
             modifiers.setAccessible( true );
             
             modifiers.set( field, field.getModifiers() & ~Modifier.FINAL );
             
             field.setAccessible( true );
             return field;
         }
         catch ( Exception e )
         {
             UnexpectedFishingMain.log( "Could not retrieve field \"" + name + "\" from class \"" + clazz.getName() + "\"" );
             e.printStackTrace();
         }
         
         return null;
     }
     
     public static Method getMethod( Class clazz, String name, Class... args )
     {
         try
         {
             Method method = null;
             
             try
             {
                if ( ( args != null ) & ( args.length != 0 ) )
                {
                    method = clazz.getMethod( name, args );
                }
                else
                {
                    method = clazz.getMethod( name, new Class[0] );
                }
             }
             catch ( Exception e )
             {
                 method = null;
             }
             
             if ( method == null )
             {
                 if ( ( args != null ) & ( args.length != 0 ) )
                 {
                     method = clazz.getDeclaredMethod( name, args );
                 }
                 else
                 {
                     method = clazz.getDeclaredMethod( name, new Class[0] );
                 }
             }
             
             method.setAccessible( true );
             return method;
         }
         catch ( Exception e )
         {
             UnexpectedFishingMain.log( "Could not retrieve method \"" + name + "\" from class \"" + clazz.getName() + "\"" );
             e.printStackTrace();
         }
         
         return null;
     }
     
     public static String methodToString( Method method )
     {
        return ( Modifier.toString( method.getModifiers() ) + " " + ( ( method.getReturnType() != null ) ? method.getReturnType().getName() : "void" + " " + method.getName() ) );
     }
 }
