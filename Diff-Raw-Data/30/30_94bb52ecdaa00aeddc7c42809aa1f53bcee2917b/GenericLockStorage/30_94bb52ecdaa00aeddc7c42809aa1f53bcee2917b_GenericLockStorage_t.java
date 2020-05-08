 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 
 import static com.flexive.core.DatabaseConst.TBL_LOCKS;
 
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.LockStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLock;
 import com.flexive.shared.FxLockType;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.security.ACLPermission;
import com.flexive.shared.security.Account;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Generic SQL based implementation of the lock storage
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericLockStorage implements LockStorage {
     private static final Log LOG = LogFactory.getLog(GenericLockStorage.class);
 
     public final static long DURATION_LOOSE = 10 * 60 * 1000; //10 min
     public final static long DURATION_PERM = 24 * 60 * 60 * 1000; //24 hr
 
 
     private static final LockStorage instance = new GenericLockStorage();
 
     /**
      * Singleton getter
      *
      * @return LockStorage
      */
     public static LockStorage getInstance() {
         return instance;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock lock(Connection con, FxLockType lockType, FxPK pk) throws FxLockException {
         switch (lockType) {
             case Loose:
                 return lock(con, lockType, pk, DURATION_LOOSE);
             case Permanent:
                 return lock(con, lockType, pk, DURATION_PERM);
         }
         throw new UnsupportedOperationException("Unsupported lock type: " + lockType.name());
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock lock(Connection con, FxLockType lockType, FxPK pk, long duration) throws FxLockException {
         return _lock(con, lockType, pk, duration);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock lock(Connection con, FxLockType lockType, String resource) throws FxLockException {
         switch (lockType) {
             case Loose:
                 return lock(con, lockType, resource, DURATION_LOOSE);
             case Permanent:
                 return lock(con, lockType, resource, DURATION_PERM);
         }
         throw new UnsupportedOperationException("Unsupported lock type: " + lockType.name());
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock lock(Connection con, FxLockType lockType, String resource, long duration) throws FxLockException {
         return _lock(con, lockType, resource, duration);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock getLock(Connection con, FxPK pk) throws FxLockException {
         return _getLock(con, pk);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock getLock(Connection con, String resource) throws FxLockException {
         return _getLock(con, resource);
     }
 
     /**
      * Resolve the distinct version of a primary key or throw an exception if no distinct version can be evaluated
      *
      * @param con an open and valid connection
      * @param pk  primary key
      * @return pk in a distinct version
      * @throws FxLockException if no distinct version could be evaluated
      */
     private FxPK getDistinctPK(Connection con, FxPK pk) throws FxLockException {
         if (pk.isDistinctVersion())
             return pk;
         final FxContentVersionInfo cvi;
         try {
             cvi = StorageManager.getContentStorage(pk.getStorageMode()).getContentVersionInfo(con, pk.getId());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         }
         if (pk.getVersion() == FxPK.LIVE) {
             if (cvi.hasLiveVersion())
                 return new FxPK(pk.getId(), cvi.getLiveVersion());
         }
         if (pk.getVersion() == FxPK.MAX)
             return new FxPK(pk.getId(), cvi.getMaxVersion());
         throw new FxLockException("ex.lock.distictPK");
     }
 
     /**
      * Internal lock method that acquires a lock for a primary key or resource depending of the class of <code>obj</code>
      *
      * @param con      an open and valid connection
      * @param lockType type of the lock
      * @param obj      resource or primary key
      * @param duration duration in [ms] of the lock
      * @return FxLock
      * @throws FxLockException     on errors
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     protected FxLock _lock(Connection con, FxLockType lockType, Object obj, long duration) throws FxLockException {
         if (obj instanceof FxPK) {
             obj = getDistinctPK(con, (FxPK) obj);
         } else if (obj instanceof String) {
             if (StringUtils.isEmpty((String) obj))
                 throw new FxLockException("ex.lock.invalidResource");
         } else
             throw new FxLockException("ex.lock.invalidResource");
         PreparedStatement ps = null;
 
         final UserTicket ticket = FxContext.getUserTicket();
         //permission checks if content lock
         if (!(ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor()) && obj instanceof FxPK)
             checkEditPermission(con, (FxPK) obj, ticket);
 
         final boolean allowTakeOver = lockType == FxLockType.Loose ||
                 (lockType == FxLockType.Permanent &&
                         (ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor()));
         final long now = System.currentTimeMillis();
 
         try {
             FxLock currentLock = _getLock(con, obj);
             if (currentLock.isLocked()) {
                 //if the lock already exist, extend it if it is from the same user or take over if allowed to
                 if (currentLock.getUserId() == ticket.getUserId())
                     return extend(con, currentLock, duration);
                 if (!allowTakeOver)
                     throw new FxLockException("ex.lock.takeOver.denied." + (obj instanceof FxPK ? "pk" : "resource"), obj);
                 return takeOver(con, currentLock, duration);
             }
             if (obj instanceof FxPK) {
                 ps = con.prepareStatement("INSERT INTO " + TBL_LOCKS +
                         // 1       2                      3       4        5          6
                         " (LOCK_ID,LOCK_VER,LOCK_RESOURCE,USER_ID,LOCKTYPE,CREATED_AT,EXPIRES_AT)VALUES(?,?,NULL,?,?,?,?)");
                 ps.setLong(1, ((FxPK) obj).getId());
                 ps.setInt(2, ((FxPK) obj).getVersion());
                 ps.setLong(3, ticket.getUserId());
                 ps.setInt(4, lockType.getId());
                 ps.setLong(5, now);
                 ps.setLong(6, now + duration);
             } else {
                 ps = con.prepareStatement("INSERT INTO " + TBL_LOCKS +
                         //                  1             2       3        4          5
                         " (LOCK_ID,LOCK_VER,LOCK_RESOURCE,USER_ID,LOCKTYPE,CREATED_AT,EXPIRES_AT)VALUES(NULL,NULL,?,?,?,?,?)");
                 ps.setString(1, (String) obj);
                 ps.setLong(2, ticket.getUserId());
                 ps.setInt(3, lockType.getId());
                 ps.setLong(4, now);
                 ps.setLong(5, now + duration);
             }
             if (ps.executeUpdate() != 1)
                 throw new FxLockException("ex.lock.lockFailed.noRows." + (obj instanceof FxPK ? "pk" : "resource"), obj);
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
         return new FxLock(lockType, now, now + duration, ticket.getUserId(), obj);
     }
 
     /**
      * Check if the calling user has edit permission on the primary key he is trying to lock
      *
      * @param con    an open and valid connection
      * @param pk     primary key of the content instance to check
      * @param ticket calling users ticket
      * @throws FxLockException thrown when the content can not be loaded/checked or no access
      */
     private void checkEditPermission(Connection con, FxPK pk, UserTicket ticket) throws FxLockException {
        if( ticket.isGuest() || ticket.getUserId() == Account.USER_GUEST )
             throw new FxLockException("ex.lock.content.guest");
         FxContent content;
         FxContentSecurityInfo si;
         FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
         if (cachedContent == null) {
             StringBuilder sb = new StringBuilder(5000);
             try {
                 final ContentStorage contentStorage = StorageManager.getContentStorage(pk.getStorageMode());
                 content = contentStorage.contentLoad(con, pk, CacheAdmin.getEnvironment(), sb);
                 si = contentStorage.getContentSecurityInfo(con, pk, content);
             } catch (FxApplicationException e) {
                 throw new FxLockException(e);
             }
         } else {
             content = cachedContent.getContent();
             si = cachedContent.getSecurityInfo();
         }
         try {
             FxPermissionUtils.checkPermission(ticket, content.getLifeCycleInfo().getCreatorId(),
                     ACLPermission.EDIT,
                     CacheAdmin.getEnvironment().getType(content.getTypeId()),
                     si.getStepACL(),
                     si.getContentACLs(), true);
         } catch (FxNoAccessException e) {
             throw new FxLockException(e, "ex.lock.content.noEditPermission", pk);
         }
     }
 
     /**
      * Internal method that returns a lock if <code>obj</code> is locked, or <code>FxLockType.None</code> if not
      *
      * @param con an open and valid connection
      * @param obj resource or primary key
      * @return FxLock or <code>FxLockType.None</code> if not
      * @throws FxLockException on errors
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     protected FxLock _getLock(Connection con, Object obj) throws FxLockException {
         if (obj instanceof FxPK) {
             obj = getDistinctPK(con, (FxPK) obj);
         } else if (obj instanceof String) {
             if (StringUtils.isEmpty((String) obj))
                 throw new FxLockException("ex.lock.invalidResource");
         } else
             throw new FxLockException("ex.lock.invalidResource");
         PreparedStatement ps = null;
         try {
             if (obj instanceof FxPK) {
                 //                                1       2        3          4
                 ps = con.prepareStatement("SELECT USER_ID,LOCKTYPE,CREATED_AT,EXPIRES_AT FROM " + TBL_LOCKS +
                         " WHERE LOCK_ID=? AND LOCK_VER=?");
                 ps.setLong(1, ((FxPK) obj).getId());
                 ps.setInt(2, ((FxPK) obj).getVersion());
             } else {
                 //                                1       2        3          4
                 ps = con.prepareStatement("SELECT USER_ID,LOCKTYPE,CREATED_AT,EXPIRES_AT FROM " + TBL_LOCKS +
                         " WHERE LOCK_RESOURCE=?");
                 ps.setString(1, (String) obj);
             }
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 return (obj instanceof FxPK ? FxLock.noLockPK() : FxLock.noLockResource());
             FxLock ret = new FxLock(FxLockType.getById(rs.getInt(2)), rs.getLong(3), rs.getLong(4), rs.getLong(1), obj);
             if (ret.isExpired()) {
                 ps.close();
                 if (ret.isContentLock()) {
                     ps = con.prepareStatement("DELETE FROM " + TBL_LOCKS + " WHERE LOCK_ID=? AND LOCK_VER=?");
                     ps.setLong(1, ((FxPK) obj).getId());
                     ps.setInt(2, ((FxPK) obj).getVersion());
                 } else {
                     ps = con.prepareStatement("DELETE FROM " + TBL_LOCKS + " WHERE LOCK_RESOURCE=?");
                     ps.setString(1, String.valueOf(obj));
                 }
                 ps.executeUpdate();
                 return (obj instanceof FxPK ? FxLock.noLockPK() : FxLock.noLockResource());
             }
             return ret;
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void unlock(Connection con, FxPK pk) throws FxLockException {
         _unlock(con, pk);
     }
 
     /**
      * {@inheritDoc}
      */
     public void unlock(Connection con, String resource) throws FxLockException {
         _unlock(con, resource);
     }
 
     /**
      * Internal unlock method to unlock a primary key or resource
      *
      * @param con an open and valid connection
      * @param obj resource or primary key
      * @throws FxLockException on errors
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     protected void _unlock(Connection con, Object obj) throws FxLockException {
         FxLock currentLock = _getLock(con, obj);
         if (!currentLock.isLocked())
             return; //nothing locked, so unlock is successful
         final UserTicket ticket = FxContext.getUserTicket();
        if( ticket.isGuest() || ticket.getUserId() == Account.USER_GUEST )
             throw new FxLockException("ex.lock.content.guest");    
         final boolean allowUnlock = currentLock.getLockType() == FxLockType.Loose ||
                 currentLock.isExpired() ||
                 (currentLock.getLockType() == FxLockType.Permanent &&
                         (ticket.getUserId() == currentLock.getUserId() ||
                                 ticket.isGlobalSupervisor() ||
                                 ticket.isMandatorSupervisor()));
         if (!allowUnlock)
             throw new FxLockException("ex.lock.unlock.denied", obj);
         PreparedStatement ps = null;
         try {
             if (currentLock.isContentLock()) {
                 obj = getDistinctPK(con, (FxPK) obj); //make sure to have a distinct pk
                 ps = con.prepareStatement("DELETE FROM " + TBL_LOCKS + " WHERE LOCK_ID=? AND LOCK_VER=?");
                 ps.setLong(1, ((FxPK) obj).getId());
                 ps.setInt(2, ((FxPK) obj).getVersion());
             } else {
                 ps = con.prepareStatement("DELETE FROM " + TBL_LOCKS + " WHERE LOCK_RESOURCE=?");
                 ps.setString(1, String.valueOf(obj));
             }
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxLock extend(Connection con, FxLock lock, long duration) throws FxLockException {
         final UserTicket ticket = FxContext.getUserTicket();
         if (lock.isExpired()) {
             final Object obj = lock.isContentLock() ? lock.getLockedPK() : lock.getLockedResource();
             return _lock(con, lock.getLockType(), obj, duration);
         }
         final boolean allowExtend = lock.getLockType() == FxLockType.Loose ||
                 (lock.getLockType() == FxLockType.Permanent &&
                         (ticket.getUserId() == lock.getUserId() ||
                                 ticket.isGlobalSupervisor() ||
                                 ticket.isMandatorSupervisor()));
         if (!allowExtend)
             throw new FxLockException("ex.lock.extend.denied." + (lock.isContentLock() ? "pk" : "resource"), (lock.isContentLock() ? lock.getLockedPK() : lock.getLockedResource()));
         PreparedStatement ps = null;
         try {
             if (lock.isContentLock()) {
                 ps = con.prepareStatement("UPDATE " + TBL_LOCKS + " SET EXPIRES_AT=? WHERE LOCK_ID=? AND LOCK_VER=?");
                 ps.setLong(2, lock.getLockedPK().getId());
                 ps.setInt(3, lock.getLockedPK().getVersion());
             } else {
                 ps = con.prepareStatement("UPDATE " + TBL_LOCKS + " SET EXPIRES_AT=? WHERE LOCK_RESOURCE=?");
                 ps.setString(2, lock.getLockedResource());
             }
             ps.setLong(1, lock.getExpiresTimestamp() + duration);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
         Object obj = lock.isContentLock() ? lock.getLockedPK() : lock.getLockedResource();
         return new FxLock(lock.getLockType(), lock.getCreatedTimestamp(), lock.getExpiresTimestamp() + duration, lock.getUserId(), obj);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLock takeOver(Connection con, FxLock lock) throws FxLockException {
         return takeOver(con, lock, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxLock takeOver(Connection con, FxLock lock, long duration) throws FxLockException {
         final UserTicket ticket = FxContext.getUserTicket();
         final boolean allowTakeOver = lock.getLockType() == FxLockType.Loose ||
                 lock.isExpired() ||
                 (lock.getLockType() == FxLockType.Permanent && (ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor()));
         if (!allowTakeOver) {
             if (lock.isContentLock())
                 throw new FxLockException("ex.lock.takeOver.denied.pk", lock.getLockedPK());
             else
                 throw new FxLockException("ex.lock.takeOver.denied.resource", lock.getLockedResource());
         }
         //permission checks if content lock
         if (!(ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor()) && lock.isContentLock())
             checkEditPermission(con, lock.getLockedPK(), ticket);
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("UPDATE " + TBL_LOCKS + " SET USER_ID=?" + (duration > 0 ? ",EXPIRES_AT=?" : "") +
                     " WHERE " + (lock.isContentLock() ? "LOCK_ID=? AND LOCK_VER=?" : "LOCK_RESOURCE=?"));
             ps.setLong(1, ticket.getUserId());
             int startIdx = 2;
             if (duration > 0) {
                 ps.setLong(2, lock.getExpiresTimestamp() + duration);
                 startIdx++;
             }
             if (lock.isContentLock()) {
                 ps.setLong(startIdx, lock.getLockedPK().getId());
                 ps.setInt(startIdx + 1, lock.getLockedPK().getVersion());
             } else
                 ps.setString(startIdx, lock.getLockedResource());
             if (ps.executeUpdate() != 1)
                 throw new FxLockException("ex.lock.takeOverFailed.noRows", lock.isContentLock() ? lock.getLockedPK() : lock.getLockedResource());
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
         Object obj = lock.isContentLock() ? lock.getLockedPK() : lock.getLockedResource();
         return new FxLock(lock.getLockType(), lock.getCreatedTimestamp(),
                 duration > 0 ? lock.getExpiresTimestamp() + duration : lock.getExpiresTimestamp(),
                 ticket.getUserId(), obj);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public List<FxLock> getUserLocks(Connection con, long userId) {
         List<FxLock> result = new ArrayList<FxLock>(10);
         PreparedStatement ps = null;
         try {
             //                                1        2          3          4       5        6
             ps = con.prepareStatement("SELECT LOCKTYPE,CREATED_AT,EXPIRES_AT,LOCK_ID,LOCK_VER,LOCK_RESOURCE FROM " + TBL_LOCKS + " WHERE USER_ID=?");
             ps.setLong(1, userId);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 Object res = rs.getString(6);
                 if (rs.wasNull())
                     res = new FxPK(rs.getLong(4), rs.getInt(5));
                 try {
                     result.add(new FxLock(FxLockType.getById(rs.getInt(1)), rs.getLong(2), rs.getLong(3), userId, res));
                 } catch (FxLockException e) {
                     LOG.warn(e);
                 }
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public List<FxLock> getLocks(Connection con, FxLockType lockType, long userId, long typeId, String resource) {
         final UserTicket ticket = FxContext.getUserTicket();
         if (!(ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor()))
             userId = ticket.getUserId();
 
         StringBuilder sql = new StringBuilder(500);
         sql.append("SELECT l.LOCKTYPE,l.CREATED_AT,l.EXPIRES_AT,l.LOCK_ID,l.LOCK_VER,l.LOCK_RESOURCE,l.USER_ID FROM ").append(TBL_LOCKS).append(" l");
         boolean hasWhere = false;
         if (typeId >= 0) {
             hasWhere = true;
             sql.append(", ").append(DatabaseConst.TBL_CONTENT).append(" c");
             sql.append(" WHERE c.ID=l.LOCK_ID AND c.VER=l.LOCK_VER AND c.TDEF=").append(typeId);
         }
         if (lockType != null) {
             if (!hasWhere) {
                 hasWhere = true;
                 sql.append(" WHERE ");
             } else
                 sql.append(" AND ");
             sql.append("l.LOCKTYPE=").append(lockType.getId());
         }
         if (userId >= 0) {
             if (!hasWhere) {
                 hasWhere = true;
                 sql.append(" WHERE ");
             } else
                 sql.append(" AND ");
             sql.append("l.USER_ID=").append(userId);
         }
         if (!StringUtils.isEmpty(resource)) {
             if (!hasWhere) {
                 sql.append(" WHERE ");
             } else
                 sql.append(" AND ");
             resource = resource.trim();
             //prevent sql injection, although only callable by global supervisors
             resource = resource.replace('\'', '_');
             resource = resource.replace('\"', '_');
             resource = resource.replace('%', '_');
             sql.append("l.LOCK_RESOURCE LIKE '%").append(resource).append("%'");
         }
         List<FxLock> result = new ArrayList<FxLock>(50);
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 Object res = rs.getString(6);
                 if (rs.wasNull())
                     res = new FxPK(rs.getLong(4), rs.getInt(5));
                 try {
                     result.add(new FxLock(FxLockType.getById(rs.getInt(1)), rs.getLong(2), rs.getLong(3), rs.getLong(7), res));
                 } catch (FxLockException e) {
                     LOG.warn(e);
                 }
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public void removeExpiredLocks(Connection con) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("DELETE FROM " + TBL_LOCKS + " WHERE EXPIRES_AT<?");
             ps.setLong(1, System.currentTimeMillis());
             int count = ps.executeUpdate();
             if (count > 0)
                 LOG.info("Expired " + count + " locks");
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(GenericLockStorage.class, null, ps);
         }
     }
 }
