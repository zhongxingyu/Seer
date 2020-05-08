 package it.grid.storm.filesystem.util;
 
 import static it.grid.storm.filesystem.swig.fs_acl.permission_flags.PERM_ALL;
 import static it.grid.storm.filesystem.swig.fs_acl.permission_flags.PERM_EXECUTE;
 import static it.grid.storm.filesystem.swig.fs_acl.permission_flags.PERM_READ_DATA;
 import static it.grid.storm.filesystem.swig.fs_acl.permission_flags.PERM_WRITE_DATA;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import it.grid.storm.filesystem.swig.fs_acl;
 import it.grid.storm.filesystem.swig.genericfs;
 import it.grid.storm.filesystem.swig.posixapi_interface;
 import it.grid.storm.filesystem.swig.storm_xattrs;
 
 public abstract class BaseFSUtil {
 	
 	genericfs fs;
 	
 	public static enum Command{
 		GET_FREE_SPACE,
 		GET_SIZE,
 		GET_EXACT_SIZE,
 		GET_LAST_MODIFICATION_TIME,
 		GET_EXACT_LAST_MODIFICATION_TIME,
 		CHANGE_GROUP_OWNERSHIP,
 		TRUNCATE,
 		PRINT_ACL,
 		SET_ACL,
 		PRINT_ATTRS,
 		SET_ATTR,
 		REMOVE_ATTR;
 	}
 	
 	protected BaseFSUtil() {
 		
 	}
 
 	protected void fileSanityChecks(String fileName)
 			throws FileNotFoundException {
 				File f = new File(fileName);
 				
 				if (!f.exists()){
 					throw new FileNotFoundException(fileName);
 				}
 			}
 
 	protected long longSanityChecks(String longStr) {
 		Long l = Long.parseLong(longStr);
 		return l;
 	}
 
 	protected boolean test(int bits, int permission) {
 		int result = bits & permission;
 		
 		return (result == permission);
 	}
 
 	protected String permissionString(int bits) {
 		
 		if ( test(bits, PERM_ALL))
 			return "ALL";
 		
 		StringBuilder str = new StringBuilder();
 		if ( test(bits, PERM_READ_DATA))
 			str.append("r");
 		else
 			str.append("-");
 		
 		if ( test(bits, PERM_WRITE_DATA) )
 			str.append("w");
 		else
 			str.append("-");
 		
 		if ( test(bits, PERM_EXECUTE) )
 			str.append("x");
 		else
 			str.append("-");
 		
 		return str.toString();	
 	}
 
 	protected void printACL(String file) {
 		
 		fs_acl acl = fs.new_acl();
 		acl.load(file);
 		
 		int ownerUid = acl.get_owner_uid();
 		int groupOwnerId = acl.get_group_owner_gid();
 		
 		String ownerName = posixapi_interface.username_from_uid(ownerUid);
 		String groupName = posixapi_interface.groupname_from_gid(groupOwnerId);
 		
 		String ownerPerms = permissionString(acl.get_owner_perm());
 		String groupOwnerPerms = permissionString(acl.get_group_owner_perm());
 		String otherPerms = permissionString(acl.get_other_perm());
 		
 		System.out.format("# file: %s\n# owner: %s\n# group: %s\n", 
 				file,
 				ownerName,
 				groupName);
 		
 		
 		System.out.format("user::%s\n", ownerPerms);
 		
 		
 		for (int uid: acl.get_uid_list()){
 			
 			int perms = acl.get_user_perm(uid);
 			String name = posixapi_interface.username_from_uid(uid);
 			System.out.format("user:%s:%s\n", 
 					name, 
 					permissionString(perms));
 		}
 		
 		System.out.format("group::%s\n", groupOwnerPerms);
 		
 		for (int gid: acl.get_gid_list()){
 			int perms = acl.get_group_perm(gid);
 			String name = posixapi_interface.groupname_from_gid(gid);
 			System.out.format("group:%s:%s\n", 
 					name, 
 					permissionString(perms));
 			
 		}
 		
 		if (acl.get_uid_list().length > 0 || acl.get_gid_list().length > 0){
 			String maskPermString  = permissionString(acl.get_mask());
 			if (!maskPermString.equals("")){
 				System.out.format("mask::%s\n", maskPermString);
 			}
 		}
 		
 		System.out.format("other:::%s\n", otherPerms);
 		
 	}
 
 	protected void executeCommand(String[] args) throws FileNotFoundException {
 		String cmd = args[0].toUpperCase().replace("-", "_");
 		Command c = Command.valueOf(cmd);
 		
 		switch(c){
 		
 		case GET_FREE_SPACE:
 			System.out.format("free space: %d\n", fs.get_free_space());
 			break;
 		case GET_SIZE:
 			argsLengthCheck(args, 2, "get-size <filename>");
 			
 			System.out.format("file size: %d\n", fs.get_size(args[1]));
 			break;
 		case GET_LAST_MODIFICATION_TIME:
 			argsLengthCheck(args, 2, "get-last-modification-time <filename>");
 			
 			System.out.format("file last modification time: %d\n", fs.get_last_modification_time(args[1]));
 			break;
 			
 		case TRUNCATE:
 			argsLengthCheck(args, 3, "truncate <filename> <size>");
 			
 			fs.truncate_file(args[1], longSanityChecks(args[2]));
 			System.out.format("file %s truncate to size: %s\n", args[1], args[2]);
 			break;
 			
 		case PRINT_ACL:
 			argsLengthCheck(args, 2, "print-acl <filename>");
 			
 			printACL(args[1]);
 			break;
 			
 		case PRINT_ATTRS:
 			argsLengthCheck(args, 2, "print-attrs <filename>");
 			printAttrs(args[1]);
 			break;
 		
 		case CHANGE_GROUP_OWNERSHIP:
 			argsLengthCheck(args, 3, "change-group-ownership <filename> <groupname>");
 			changeGroupOwnership(args[1], args[2]);
			break;
 			
 		default:
 			throw new IllegalArgumentException("Unsupported command! "+args[0]);
 		}
 		
 		System.exit(0);
 		
 	}
 
 	protected void changeGroupOwnership(String filename, String groupname) {
 		
 		fs.change_group_ownership(filename, groupname);
 		
 	}
 
 	protected void printAttrs(String file) {
 		
 		List<String> attrNames = storm_xattrs.get_xattr_names(file);
 		
 		for (String attrName: attrNames){
 			
 			String attrValue = storm_xattrs.get_xattr_value(file, attrName);
 			System.out.format("%s : %s\n", attrName, attrValue);
 		}
 	}
 
 	protected void argsLengthCheck(String[] args, int lenght, String message) {
 		if (args.length < lenght){	
 			System.err.format("Usage: FSUtil %s", message);
 			System.exit(1);
 		}
 	}
 
 }
