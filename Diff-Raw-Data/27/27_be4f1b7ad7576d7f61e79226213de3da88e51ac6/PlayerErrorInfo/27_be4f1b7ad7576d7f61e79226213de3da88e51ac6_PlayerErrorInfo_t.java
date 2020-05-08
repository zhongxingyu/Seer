 package com.dbstar.DbstarDVB.VideoPlayer.alert;
 
 import com.dbstar.DbstarDVB.R;
 import android.content.res.Resources;
 
 public class PlayerErrorInfo {
 	public static final int CDCA_RC_CARD_INVALID = 0x03;
 	public static final int CDCA_RC_FILE_ERROR = 0x30;
 	public static final int CDCA_RC_NOENTITLE = 0x32;
 	public static final int CDCA_RC_NOT_ISSUETIME = 0x33;
 	public static final int CDCA_RC_NOT_WATCHTIME = 0x34;
 	public static final int CDCA_RC_RIGHT_LIMIT = 0x35;
 
	
	public static boolean isDRMError(int errorCode) {
		return (errorCode == CDCA_RC_CARD_INVALID || 
				errorCode == CDCA_RC_FILE_ERROR ||
				errorCode == CDCA_RC_NOENTITLE ||
				errorCode == CDCA_RC_NOT_ISSUETIME ||
				errorCode == CDCA_RC_NOT_WATCHTIME ||
				errorCode == CDCA_RC_RIGHT_LIMIT);
	}
	
	
 	public static String getErrorString(Resources res, int code) {
 		String errStr = null;
 		switch(code) {
 		case CDCA_RC_FILE_ERROR: {
 			errStr = res.getString(R.string.drm_error_fileerror);
 			break;
 		}
 		case CDCA_RC_NOENTITLE: {
 			errStr = res.getString(R.string.drm_error_noentitle);
 			break;
 		}
 		case CDCA_RC_NOT_ISSUETIME: {
 			errStr = res.getString(R.string.drm_error_notissuetime);
 			break;
 		}
 		case CDCA_RC_NOT_WATCHTIME: {
 			errStr = res.getString(R.string.drm_error_notwatchtime);
 			break;
 		}
 		case CDCA_RC_RIGHT_LIMIT: {
 			errStr = res.getString(R.string.drm_error_rightlimit);
 			break;
 		}
 		case CDCA_RC_CARD_INVALID: {
 			errStr = res.getString(R.string.drm_error_card_invalid);
 			break;
 		}
 		default: {
 			errStr = res.getString(R.string.error_unknown);
 			break;
 		}
 		}
 		
 		return errStr;
 	}
 }
