 package org.fusesource.jlibpam.internal;
 
 import static org.fusesource.hawtjni.runtime.ArgFlag.*;
 import static org.fusesource.hawtjni.runtime.ClassFlag.*;
 import static org.fusesource.hawtjni.runtime.FieldFlag.*;
 import static org.fusesource.hawtjni.runtime.MethodFlag.*;
 import static org.fusesource.hawtjni.runtime.Pointer.*;
 
 import org.fusesource.hawtjni.runtime.*;
 
 @JniClass(conditional="defined(HAVE_SECURITY_PAM_APPL_H)")
 public class PamLibrary {
 	
 	public static final Library LIBRARY = new Library("jlibpam", PamLibrary.class);
 	static {
 		LIBRARY.load();
 		init();
 	}
 
     @JniMethod(flags={CONSTANT_INITIALIZER})
     public static final native void init();
 
     @JniField(flags={CONSTANT})
     public static short PAM_AUTHTOK;
 
 	public static final native int pam_acct_mgmt(
 		@JniArg(cast="pam_handle_t *") long pamh,
 		@JniArg int flags);
 
 	public static final native int pam_authenticate(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int flags);
 
 	public static final native int pam_chauthtok(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int flags);
 
 	public static final native int pam_close_session(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int flags);
 
 	public static final native int pam_end(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int status);
 
 /*
 	public static final native int pam_get_data(
 			@JniArg(cast="const pam_handle_t *") long pamh,
 			const char *module_data_name,
 		 	const void **data);
 
 	public static final native int pam_get_item(
 			@JniArg(cast="const pam_handle_t *") long pamh,
 			int item_type, 
 			const void **item);
 
 	public static final native int pam_get_user(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			const char **user, 
 			const char *prompt);
 */
 	@JniMethod(accessor="pam_getenv")
     public static final native long __pam_getenv(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg(cast="const char *", flags=CRITICAL) String name);
 			
 	public static final String pam_getenv(long pamh, String name) {
 		return toString(__pam_getenv(pamh, name));
 	}
 
 /*
      char ** pam_getenvlist(
 			@JniArg(cast="pam_handle_t *") long pamh);
 */
 	public static final native int pam_open_session(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int flags);
 
 	public static final native int pam_putenv(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg(cast="const char *", flags=CRITICAL) String namevalue);
 
 	public static final native int pam_set_data(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg(cast="const char *", flags=CRITICAL) String module_data_name,
 		 	@JniArg(cast="void *") long data,
		 	@JniArg(cast="void (*cleanup)(pam_handle_t *pamh, void *data, int pam_end_status)") long cleanup);
 
     // TODO: the third argument may be a pam_conv struct
 	public static final native int pam_set_item(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int item_type, 
 			@JniArg(cast="const void *", flags=CRITICAL) String item);
 
 	public static final native int pam_setcred(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			int flags);
 
 	public static final native int pam_start(
 			@JniArg(cast="const char *", flags=CRITICAL) String service, 
 			@JniArg(cast="const char *", flags=CRITICAL) String user, 
		 	@JniArg pam_conv pam_conv, 
 			@JniArg(cast="pam_handle_t **", flags={NO_IN, CRITICAL}) long[] pamh);
 
 	@JniMethod(accessor="pam_strerror")
 	public static final native long __pam_strerror(
 			@JniArg(cast="pam_handle_t *") long pamh,
 			@JniArg int error_number
 			);
 			
 	public static final String pam_strerror(long pamh, int error_number) {
 		return toString(__pam_strerror(pamh, error_number));
 	}
 
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(HAVE_SECURITY_PAM_APPL_H)")
     public static class pam_conv { 
         static {
             LIBRARY.load();
             init();
         }
         
         @JniMethod(flags={CONSTANT_INITIALIZER})
         public static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(pam_conv)")
         public static int SIZEOF;
 
 	}
 
     public static final native void memmove(
             @JniArg(cast="void *", flags={NO_OUT, CRITICAL}) byte[] dest, 
             @JniArg(cast="const void *") long src, 
             @JniArg(cast="size_t") long size);
 	
 	public static final native int strlen(
 			@JniArg(cast="const char *") long ptr);
 
 	public static String toString(long ptr) {
 		if (ptr == 0) {
 			return null;
 		}
 		int length = strlen(ptr);
 		byte[] data = new byte[length];
 		memmove(data, ptr, length);
 		return new String(data);
 	}
 
 }
