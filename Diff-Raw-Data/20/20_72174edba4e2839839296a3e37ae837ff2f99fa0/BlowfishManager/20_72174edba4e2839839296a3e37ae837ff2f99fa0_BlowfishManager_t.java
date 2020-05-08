 /**
  * Copyright 2009, Frederic Bregier, and individual contributors
  * by the @author tags. See the COPYRIGHT.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3.0 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package goldengate.common.crypto;
 
 import java.util.List;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * This class implements a simple Key Manager for Blowfish class from name
  *
  * @author frederic bregier
  */
 public class BlowfishManager extends KeyManager {
     public static final BlowfishManager blowfishManager = new BlowfishManager();
     private static final ReentrantLock lock = new ReentrantLock();
 
     /**
     * FIXME Method is not finished!
      * Initialize the DesManager from the list of key names
      * @param keys
      */
    @Deprecated
     public static void initialize(List<String> keys) {
         if (blowfishManager.isInitialized.get()) {
             return; // already initialized
         }
         lock.lock();
         try {
             // Double check in case between first and second check another thread make the init
             if (blowfishManager.isInitialized.get()) {
                 return; // already initialized
             }
             String extension = Blowfish.EXTENSION;
             List<String> wrong = blowfishManager.initFromList(keys, extension);
             // FIXME handle if necessary wrong keys
             // ...
         } finally {
             lock.unlock();
         }
     }
 
     /* (non-Javadoc)
      * @see atlas.cryptage.KeyManager#createKeyObject()
      */
     @Override
     public KeyObject createKeyObject() {
         return new Blowfish();
     }
 }
