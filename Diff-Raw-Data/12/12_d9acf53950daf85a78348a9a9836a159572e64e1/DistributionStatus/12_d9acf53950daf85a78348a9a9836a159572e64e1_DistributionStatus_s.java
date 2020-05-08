 // Copyright (C) 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.communication;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import net.grinder.communication.ConnectionIdentity;
 
 
 /**
  * Record of the distribution status of the worker processes.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class DistributionStatus {
 
   private final Map m_connectionToLastModifiedMap = new WeakHashMap();
 
   /**
    * Report a new last modified time for a connection.
    *
    * @param connectionIdentity The connection identity.
    * @param lastModifiedTime The last modified time.
    */
   public void set(ConnectionIdentity connectionIdentity,
                   long lastModifiedTime) {
 
     synchronized (m_connectionToLastModifiedMap) {
       m_connectionToLastModifiedMap.put(connectionIdentity,
                                         new Long(lastModifiedTime));
     }
   }
 
   /**
    * Remove data for a connection.
    *
    * @param connectionIdentity The connection identity.
    */
   public void remove(ConnectionIdentity connectionIdentity) {
     synchronized (m_connectionToLastModifiedMap) {
       m_connectionToLastModifiedMap.remove(connectionIdentity);
     }
   }
 
   /**
    * Get the earliest last modified time.
    *
   * @return The earliest last modified time, or <code>-1</code> if we
   * have no information.
    */
   public long getEarliestLastModifiedTime() {
    long result = -1;
 
     synchronized (m_connectionToLastModifiedMap) {
       final Iterator iterator =
         m_connectionToLastModifiedMap.values().iterator();
 
       while (iterator.hasNext()) {
         final Long value = (Long) iterator.next();
 
        if (value != null &&
            (result == -1 || value.longValue() < result)) {
           result = value.longValue();
         }
       }
     }
 
     return result;
   }
 
   /**
    * In the future, we'll set individual values. For now we set all.
    *
    * @param lastModifiedTime The new last modified time.
    */
   public void setAll(long lastModifiedTime) {
 
     final Long v = new Long(lastModifiedTime);
 
     synchronized (m_connectionToLastModifiedMap) {
       final Iterator iterator =
         m_connectionToLastModifiedMap.entrySet().iterator();
 
       while (iterator.hasNext()) {
         final Map.Entry entry = (Map.Entry)iterator.next();
         entry.setValue(v);
       }
     }
 
   }
 }
