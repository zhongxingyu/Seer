 package ch.bfh.ti.soed.white.mhc_pms.security;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import ch.bfh.ti.soed.white.mhc_pms.data.enums.UserGroup;
 
 /**
  * This class sets the permissions about the allowed operations for a specific
  * user group in the MHC PMS application.
  * 
  * @author Group White, I2p, BFH Berne, <a href="https://github.com/fabaff/ch.bfh.bti7081.s2013.white">Contact</a>
  * @version 1.0.0
  */
 public class PmsPermission {
 
 	/**
 	 * This enum defines all possible operations in the MHC PMS application which needs explicit permissions.
 	 */
 	public static enum Operation {
 		DELETE_DIAGNOSIS, DELETE_MEDICATION, DELETE_PATIENT_PROGRESS_ENTRY, EDIT_CASE, EDIT_DIAGNOSIS, 
 		EDIT_MEDICATION, EDIT_PATIENT_PROGRESS_ENTRY, NEW_CASE, NEW_DIAGNOSIS, NEW_MEDICATION, 
 		NEW_PATIENT, NEW_PATIENT_PROGRESS_ENTRY
 	}
 
 	/**
 	 * This inner helper class defines which user group has a permission for an specific operation.
 	 */
 	private static class PermissionKey {
 
 		private Operation elementName;
 		private UserGroup userGroup;
 
 		/**
 		 * Element for handling the permission
 		 * 
 		 * @param userGroup
 		 * @param elementName
 		 */
 		private PermissionKey(UserGroup userGroup, Operation elementName) {
 			this.userGroup = userGroup;
 			this.elementName = elementName;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			PermissionKey other = (PermissionKey) obj;
 			if (elementName != other.elementName) {
 				return false;
 			}
 			if (userGroup != other.userGroup) {
 				return false;
 			}
 			return true;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((elementName == null) ? 0 : elementName.hashCode());
 			result = prime * result
 					+ ((userGroup == null) ? 0 : userGroup.hashCode());
 			return result;
 		}
 
 	}
 
 	private static Set<PermissionKey> permissionsSet = new HashSet<PermissionKey>();
 
	// Initialisation of all allowed operations for a specific user group
 	static {
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.NEW_MEDICATION));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.EDIT_MEDICATION));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.DELETE_MEDICATION));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.NEW_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.EDIT_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.DELETE_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.NEW_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.EDIT_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.DELETE_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.EDIT_CASE));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.NEW_CASE));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHIATRIST,
 				Operation.NEW_PATIENT));
 
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.NEW_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.EDIT_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.DELETE_DIAGNOSIS));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.NEW_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.EDIT_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.DELETE_PATIENT_PROGRESS_ENTRY));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.EDIT_CASE));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.NEW_CASE));
 		permissionsSet.add(new PermissionKey(UserGroup.PSYCHOLOGIST,
 				Operation.NEW_PATIENT));
 
 		permissionsSet.add(new PermissionKey(UserGroup.ADMIN_STAFF,
 				Operation.NEW_CASE));
 		permissionsSet.add(new PermissionKey(UserGroup.ADMIN_STAFF,
 				Operation.NEW_PATIENT));
 	}
 
 	private UserGroup userGroup;
 
 	/**
 	 * A permission object for the given user group will be created.
 	 * 
 	 * @param userGroup
 	 */
 	public PmsPermission(UserGroup userGroup) {
 		this.userGroup = userGroup;
 	}
 
 	/**
 	 * Switch for the permission
 	 * 
 	 * @param elementName
 	 * @return true, if the user group has the permission for this operation
 	 */
 	public boolean hasPermission(Operation elementName) {
 		return permissionsSet.contains(new PermissionKey(this.userGroup,
 				elementName));
 	}
 }
