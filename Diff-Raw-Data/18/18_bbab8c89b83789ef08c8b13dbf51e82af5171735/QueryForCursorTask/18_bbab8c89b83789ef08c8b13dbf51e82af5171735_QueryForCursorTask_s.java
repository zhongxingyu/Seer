 package hu.velorum.ConCopy.backend.tasks;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import hu.velorum.ConCopy.backend.entity.QueryParams;
 import hu.velorum.ConCopy.backend.interfaces.TaskUpdateInterface;
 import hu.velorum.ConCopy.backend.statics.StaticData;
 
 
 public class QueryForCursorTask extends AbstractUpdateTask<Cursor, Long> {
 
 	protected ContentResolver contentResolver;
 	private QueryParams params;
 
 	public QueryForCursorTask(TaskUpdateInterface<Cursor> taskFace, QueryParams params) {
 		super(taskFace);
 		this.params = params;
 		contentResolver = taskFace.getMeContext().getContentResolver();
 	}
 
 	@Override
 	protected Integer doTheTask(Long... ids) {
 		Uri uri = params.getUri();
 
 
 		if (ids.length > 0) {
 			int cnt = ids.length;
 			String[] arguments = new String[cnt];
 			StringBuilder stringBuilder = new StringBuilder();
 			for (int i = 0; i < cnt; i++) {
 				stringBuilder.append(params.getSelection());
 				arguments[i] = String.valueOf(ids[i]);
 			}
 			item = contentResolver.query(uri, params.getProjection(), stringBuilder.toString(), arguments, params.getOrder());
 
 			item.moveToFirst();
 		} else {
 			item = contentResolver.query(uri, params.getProjection(), params.getSelection(), params.getArguments(), params.getOrder());
 		}
 
 		if (item != null && item.moveToFirst()) {
 			result = doAdditionToCursor(item);
 		} else {
 			result = StaticData.VALUE_DOESNT_EXIST;
 		}
 		return result;
 	}
 
 	protected int doAdditionToCursor(Cursor cursor) {
 		return StaticData.RESULT_OK;
 	}
 
 }
