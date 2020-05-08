 package ru.elifantiev.fga.widget;
 
 import ru.elifantiev.fga.FuckinGreatAdvice;
 import ru.elifantiev.fga.R;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.widget.RemoteViews;
 
 public class WidgetUpdater implements Runnable
 {
     private final Context context;
     private final AppWidgetManager appWidgetManager;
     private final int widgetID;
 
     public WidgetUpdater( final Context context, final AppWidgetManager appWidgetManager,
             final int id )
     {
         this.context = context;
         this.appWidgetManager = appWidgetManager;
         this.widgetID = id;
     }
 
     @Override
     public void run()
     {
         // show loading message
        appWidgetManager.updateAppWidget( widgetID, buildWidget( context
                .getString( R.string.gettingAdvice ) ) );
 
         // fetch advice
         FuckinGreatAdvice advice = new FuckinGreatAdvice( context.getString( R.string.adviceUrl ),
                 context.getString( R.string.error ) );
 
         // update widget with advice
         appWidgetManager.updateAppWidget( widgetID, buildWidget( advice.getAdvice() ) );
     }
 
     private RemoteViews buildWidget( final String advice )
     {
         // get widget layout
         RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget );
 
         // set text
         views.setTextViewText( R.id.widget_text, advice );
         return views;
     }
 
 }
