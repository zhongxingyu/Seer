 package mx.udlap.sofia;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 import android.content.ComponentName;
 import android.content.Context;
 import android.view.View;
 
 public class Sofia_Widget extends AppWidgetProvider {
 
 	public int conjuntos_hechos;
 	public int conjuntos_total;
 	public int ecuaciones_total;
 	public int ecuaciones_hechos;
 	public int triangulos_hechos;
 	public int triangulos_total;
 	
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
 
 		/*Acceso a la base de datos*/
 		SQLiteHelper usdbh = new SQLiteHelper(context, "Base de datos", null, 2);
 		SQLiteDatabase db = usdbh.getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT * FROM ejercicios", null);
 		if (c.moveToFirst() != false) {
 			this.conjuntos_total = c.getInt(1);
 			this.conjuntos_hechos = c.getInt(2);
 		}
 		if (c.moveToNext()) {
 			this.triangulos_total = c.getInt(1);
 			this.triangulos_hechos = c.getInt(2);
 		}
 		if (c.moveToNext()) {
 			this.ecuaciones_total = c.getInt(1);
 			this.ecuaciones_hechos = c.getInt(2);
 		}
 
 		db.close();
 		/*-------------------------*/
 		
 
 		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
 		remoteViews.setTextViewText(R.id.widget_ecuaciones, "Ecuaciones:"+ecuaciones_hechos+"/"+ecuaciones_total);
 		remoteViews.setTextViewText(R.id.widget_conjuntos, "Conjuntos:"+conjuntos_hechos+"/"+conjuntos_total);
 		remoteViews.setTextViewText(R.id.widget_triangulos, "Triangulos:"+triangulos_hechos+"/"+triangulos_total);
 
 		pushWidgetUpdate(context, remoteViews);
 	}
 	
 	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
 		ComponentName myWidget = new ComponentName(context, Sofia_Widget.class);
 	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
 	    manager.updateAppWidget(myWidget, remoteViews);		
 	}
 	
 	
 
 }
