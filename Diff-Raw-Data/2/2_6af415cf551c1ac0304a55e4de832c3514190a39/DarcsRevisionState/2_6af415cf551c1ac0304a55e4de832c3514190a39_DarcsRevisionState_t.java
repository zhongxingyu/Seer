 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.jenkinsci.plugins.darcs;
 
 import hudson.scm.SCMRevisionState;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  *
  * Feedback from mailing list:
  * stephen.alan.connolly@gmail.com:
  * <quote>
  * I think you would be better served by computing the sha1 or md5 of all the hashes as strings.  Relying on Collections.hashCode is dangerous.  Relying on String.hashCode, which is just:
  *
  *     public int hashCode() {
  *         int h = hash;
  *         if (h == 0) {
  *             int off = offset;
  *             char val[] = value;
  *             int len = count;
  *
  *             for (int i = 0; i < len; i++) {
  *                 h = 31*h + val[off++];
  *             }
  *             hash = h;
  *         }
  *         return h;
  *     }
  *
  * Is going to be a tad weak given that all the characters are from the set [0-9a-f-] i.e. there are only 17 out of 255.
  *
  * The List.hashCode speci is
  *     public int hashCode() {
  *         int hashCode = 1;
  *         Iterator<E> i = iterator();
  *         while (i.hasNext()) {
  *             E obj = i.next();
  *             hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
  *         }
  *         return hashCode;
  *     }
  *
  * Also I would recommend sorting the lists before hashing them.
  *
  * However, if you are looking for a short path to saying there is a difference, the Collections.sort(list1); Collections.sort(list2); if (list1.hashCode() != list2.hashCode()) check should be OK...
  *
  * Just remember that list1.hashCode() == list2.hashCode() does not in anyway claim that there is no change, so you will have to go down the long path anyway.
  * </quote>
  *
  * kkawaguchi@cloudbees.com:
  * <quote>
  * There's utility code in Jenkins that computes MD5 digest of arbitrary byte stream or string. I think that seems like a good and cheap enough hashing for this kind of purpose.
  *
  * Javadoc
  * hudson.Util.getDigestOf()
  * public static String getDigestOf(InputStream source)
  *                          throws IOException
  *
  *    Computes MD5 digest of the given input stream.
  * </quote>
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsRevisionState extends SCMRevisionState {
 
     private DarcsChangeSetList changes;
 
     public DarcsRevisionState(DarcsChangeSetList changes) {
         super();
         this.changes = changes;
     }
 
     public DarcsChangeSetList getChanges() {
         return changes;
     }
 
     @Override
     public String toString() {
         return "<RevisionState: " + getChanges().digest() + ">";
     }
 
     @Override
     public boolean equals(Object other) {
         boolean result = false;
 
         if (other instanceof DarcsRevisionState) {
             DarcsRevisionState that = (DarcsRevisionState) other;
            return getChanges().equals(that.getChanges());
         }
 
         return result;
     }
 }
