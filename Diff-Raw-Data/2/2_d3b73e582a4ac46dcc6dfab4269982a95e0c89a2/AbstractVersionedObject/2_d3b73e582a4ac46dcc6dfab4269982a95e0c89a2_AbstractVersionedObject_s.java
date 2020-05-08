 package org.melati.util;
 
 public abstract class AbstractVersionedObject
     extends CacheNode           // well, this IS what it's going to be used for
                                 // (funny how you want to use
                                 // multip. inher. more in Java than in C++)
     implements CachedVersionedObject {
 
   private static final Version unknown =
       new Version() {
         public Object clone() { return this; }
       };
 
   private boolean valid = true;
 
   // `committedVersion == unknown' iff we don't have the committed version
   // cached, and it needs recomputing.  `committedVersion' is never actually
   // `null'.
 
   private Version committedVersion = unknown;
 
   // `(seenMask & session.mask()) != 0' iff `session' has seen the current
   // `committedVersion' and must be shielded from future changes
 
   private int seenMask = 0;
 
   // `(touchedMask & session.mask()) != 0' iff `session' has changed
   // its version of the data.
 
   private int touchedMask = 0;
 
   // `versions[session.index] == version (!= null)' iff `session' has its own
   // copy of this row independent of `committedVersion'.  `version' can be
   // `unknown', in which case its value must be recomputed before it can be
   // used.  Different sessions' versions can alias each other provided they
   // have only so far been read and not written.
   //
   // Invariants:
   //
   // (touchedMask & session.mask) != 0 => versions[session.index] != null
 
   private Version[] versions = null;
 
   // must be called synchronized (this)
 
   private void notifyTouched(Session session) {
     if ((touchedMask & session.mask()) == 0) {
       session.notifyTouched(this);
       touchedMask |= session.mask();
     }
   }
 
   // must be called synchronized (this)
 
   private void notifySeen(Session session) {
     if ((seenMask & session.mask()) == 0) {
       session.notifySeen(this);
       seenMask |= session.mask();
     }
   }
 
   protected int seenMask() {
     return seenMask();
   }
 
   /**
    * Mark a session's version as unknown if it's not just defaulting to the
    * committed version.  This is used when we want to stop storing a
    * session-local version, but need to remember that it was different from the
    * committed version so that if the session subsequently accesses it, we know
    * to recompute the session-local value rather than assuming that's the same
    * as the committed value.
    */
 
   public synchronized void uncacheVersion(Session session) {
     if (session == null)
       committedVersion = unknown;
     else if (versions != null) {
       Version version = versions[session.index()];
       if (version != null) versions[session.index()] = unknown;
     }
   }
 
   /**
    * Mark a session's version as unknown, even if it hasn't been seen or
    * touched and so is currently defaulting to the committed version.
    */
 
   public synchronized void invalidateVersion(Session session) {
     if (session == null)
       committedVersion = unknown;
     else {
       if (versions == null)
         versions = new Version[sessionsMax()];
       versions[session.index()] = unknown;
       notifySeen(session);
     }
   }
 
   // must be called `synchronized(this)'
 
   private void setCommittedVersion(Version toCommit) {
     // Give any sessions that need it the old committed version to see
     // again in future, even if it's `unknown' or `nonexistent'.
 
     if (seenMask != 0)
       for (int s = 0, mask = 1; s < versions.length; ++s, mask <<= 1)
         if ((seenMask & mask) != 0 && versions[s] == null)
           versions[s] = committedVersion;
 
     // Commit the new version, even if it's `unknown' or `nonexistent'
 
     committedVersion = toCommit;
   }
 
   /**
    * Set a session's version to a given value.  The version is scheduled for
    * write-down when the session is next committed (or some other operation
    * which provokes a write-down is performed).
    */
 
   public synchronized void setVersion(Session session, Version version) {
     assertValid();
 
     if (version == null)
       throw new NullPointerException();
 
     if (session == null)
       setCommittedVersion(version);
     else {
       if (versions == null)
         versions = new Version[sessionsMax()];
       versions[session.index()] = version;
 
       notifyTouched(session);
     }
   }
 
   protected abstract Version backingVersion(Session session);
   protected abstract int sessionsMax();
 
   /**
    * Retrieve a session's version for read access.
    *
    * @param session     The session whose version you want to see.
    * 
    * @return The session's version, fetched if necessary using
    *         <TT>backingVersion</TT>.  It may well be aliased to other
    *         sessions' versions---if you want an independent copy you can
    *         change, use <TT>versionForWriting</TT>.
    *
    * @see #versionForWriting
    */
 
   public synchronized Version versionForReading(Session session) {
     assertValid();
 
     if (session != null && versions != null) {
       Version version = versions[session.index()];
       if (version == unknown)
         return versions[session.index()] = backingVersion(session);
       else if (version != null)
         return version;
     }
 
     if (committedVersion == unknown)
       committedVersion = backingVersion(null);
 
     if (session != null)
       notifySeen(session);
 
     return committedVersion;
   }
 
   /**
    * Retrieve a session's version for write access.  The version is cloned if
    * necessary to ensure that it isn't aliasing other sessions's versions, and
    * scheduled for write-down when the session is next committed (or some other
    * operation which provokes a write-down is performed).  If you just want to
    * read the version, use <TT>versionForReading</TT>.
    *
    * @param session     The session whose version you want to update.
    * 
    * @return An independent copy of the session's version, fetched if
    *         necessary using <TT>backingVersion</TT>.
   *         to other sessions' versions---if you want an independent copy you
   *         can change, use <TT>versionForWriting</TT>.
    *
    * @see #versionForReading
    */
 
   public synchronized Version versionForWriting(Session session) {
     assertValid();
 
     Version version = versionForReading(session);
 
     if ((touchedMask & session.mask()) == 0) {
       if (versions == null)
         versions = new Version[sessionsMax()];
       version = versions[session.index()] = (Version)version.clone();
 
       notifyTouched(session);
     }
 
     return version;
   }
 
   /**
    * Mark a version as defaulting to the committed version.  Sessions do this
    * when they are committed or rolled back.
    */
 
   public synchronized void unSee(Session session) {
     seenMask &= session.negMask();
     touchedMask &= session.negMask();
     if (seenMask == 0)
       versions = null;
     else if (versions != null)
       versions[session.index()] = null;
   }
 
   protected abstract void writeDown(Session session, Version version);
 
   /**
    * Write a version down into the backing store.
    */
 
   public void writeDown(Session session) {
     Version version = null;
 
     synchronized (this) {
       if ((touchedMask & session.mask()) != 0)
         version = versions[session.index()];
     }
 
     if (version != null && version != unknown)
       writeDown(session, version);
   }
 
   /**
    * Mark the committed version as being the that of the given session.
    * Sessions may do this after a successful write-down.
    */
 
   public synchronized void commit(Session session) {
     if (versions != null) {
       if ((touchedMask & session.mask()) == 0)
         throw new IllegalArgumentException("committing touched version");
       seenMask &= session.negMask();
       Version toCommit = versions[session.index()];
       if (toCommit != null) {
         versions[session.index()] = null;
         setCommittedVersion(toCommit);
       }
     }
   }
 
   // 
   // ===========
   //  Utilities
   // ===========
   // 
 
   public synchronized int analyseContents() {
     int size = committedVersion == unknown ? 0 : 1;
     if (versions != null) {
       int versionsThere = 0;
       for (int i = 0; i < versions.length; ++i) {
         Version version = versions[i];
         if (version != null) {
           ++versionsThere;
           if (version != unknown)
             ++size;
         }
       }
       if (versionsThere == 0)
         System.err.println("*** ERROR " + this + " has an empty `versions'");
     }
 
     return size;
   }
 
   // 
   // ===========
   //  CacheNode
   // ===========
   // 
 
   public synchronized void uncacheContents() {
     committedVersion = unknown;
     if (versions != null)
       for (int v = 0; v < versions.length; ++v)
         if (versions[v] != null)
           versions[v] = unknown;
   }
 
   public synchronized boolean drop() {
     if (seenMask() == 0) {
       valid = false;
       return true;
     }
     else
       return false;
   }
 
   public boolean valid() {
     return valid;
   }
 
   private void assertValid() {
     if (!valid)
       throw new IllegalArgumentException(
           "using invalid AbstractVersionedObject");
   }
 }
