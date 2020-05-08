 
 package org.inftel.ssa.mobile;
 
 import org.inftel.ssa.mobile.provider.SsaContract;
 import org.inftel.ssa.mobile.provider.SsaContract.Projects;
 import org.inftel.ssa.mobile.provider.SsaContract.Sprints;
 import org.inftel.ssa.mobile.provider.SsaContract.Tasks;
 import org.inftel.ssa.mobile.provider.SsaContract.Users;
 
 import android.app.Application;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.util.Log;
 
 public class SsaApplication extends Application {
 
     private static final String TAG = "SsaApplication";
 
     @Override
     public final void onCreate() {
         super.onCreate();
         Log.d(TAG, "OnCreate application.");
 
         // Algunos datos para hacer pruebas
         ContentResolver cr = getContentResolver();

        if (cr.query(SsaContract.Sprints.CONTENT_URI, new String[] {
                 Sprints.SPRINT_SUMMARY
        }, null, null, null).getCount() == 0) {
 
             ContentValues values = new ContentValues();
 
             // Sprints
             values.put(Sprints.SPRINT_PROJECT_ID, 1);
 
             values.put(Sprints.SPRINT_SUMMARY, "Primer sprint");
             cr.insert(Sprints.CONTENT_URI, values);
             values.put(Sprints.SPRINT_SUMMARY, "Segundo sprint");
             cr.insert(Sprints.CONTENT_URI, values);
             values.put(Sprints.SPRINT_SUMMARY, "Tercer sprint");
             cr.insert(Sprints.CONTENT_URI, values);
 
             // Projects
             values.clear();
             values.put(Projects.PROJECT_NAME, "Manhatan");
             values.put(Projects.PROJECT_SUMMARY, "Primer projecto");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.clear();
             values.put(Projects.PROJECT_NAME, "Increible");
             values.put(Projects.PROJECT_SUMMARY, "Segundo projecto");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.clear();
             values.put(Projects.PROJECT_NAME, "Desastroso");
             values.put(Projects.PROJECT_SUMMARY, "Tercer projecto");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.clear();
 
             values.put(Projects.PROJECT_NAME, "Proyecto 1");
             values.put(Projects.PROJECT_SUMMARY, "Pasos");
             values.put(Projects.PROJECT_DESCRIPTION, "Gestion de alarmas para ancianos");
             values.put(Projects.PROJECT_OPENED, "12/3/2012");
             values.put(Projects.PROJECT_STARTED, "12/3/2012");
             values.put(Projects.PROJECT_CLOSE, "12/3/2012");
             values.put(Projects.PROJECT_COMPANY, "Inftel");
             values.put(Projects.PROJECT_LINKS, "www.inftel.com");
             values.put(Projects.PROJECT_LABELS, "");
             values.put(Projects.PROJECT_LICENSE, "GPL");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.put(Projects.PROJECT_NAME, "Proyecto 2");
             values.put(Projects.PROJECT_SUMMARY, "Centro Medico");
             values.put(Projects.PROJECT_DESCRIPTION, "Gestion del centro ambulatorio");
             values.put(Projects.PROJECT_OPENED, "12/3/2012");
             values.put(Projects.PROJECT_STARTED, "12/3/2012");
             values.put(Projects.PROJECT_CLOSE, "12/3/2012");
             values.put(Projects.PROJECT_COMPANY, "Inftel");
             values.put(Projects.PROJECT_LINKS, "www.inftel.com");
             values.put(Projects.PROJECT_LABELS, "");
             values.put(Projects.PROJECT_LICENSE, "GPL");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.put(Projects.PROJECT_NAME, "Proyecto 3");
             values.put(Projects.PROJECT_SUMMARY, "Centro de Reparaciones");
             values.put(Projects.PROJECT_DESCRIPTION, "Control de inventario");
             values.put(Projects.PROJECT_OPENED, "12/3/2012");
             values.put(Projects.PROJECT_STARTED, "12/3/2012");
             values.put(Projects.PROJECT_CLOSE, "12/3/2012");
             values.put(Projects.PROJECT_COMPANY, "Inftel");
             values.put(Projects.PROJECT_LINKS, "www.inftel.com");
             values.put(Projects.PROJECT_LABELS, "");
             values.put(Projects.PROJECT_LICENSE, "GPL");
             cr.insert(Projects.CONTENT_URI, values);
 
             values.put(Projects.PROJECT_NAME, "Proyecto 4");
             values.put(Projects.PROJECT_SUMMARY, "Central Eolica");
             values.put(Projects.PROJECT_DESCRIPTION, "Gestion de recursos");
             values.put(Projects.PROJECT_OPENED, "12/3/2012");
             values.put(Projects.PROJECT_STARTED, "12/3/2012");
             values.put(Projects.PROJECT_CLOSE, "12/3/2012");
             values.put(Projects.PROJECT_COMPANY, "Inftel");
             values.put(Projects.PROJECT_LINKS, "www.inftel.com");
             values.put(Projects.PROJECT_LABELS, "");
             values.put(Projects.PROJECT_LICENSE, "GPL");
             cr.insert(Projects.CONTENT_URI, values);
 
             // Tasks
             values.clear();
             values.put(Tasks.TASK_USER_ID, "-100");
             values.put(Tasks.TASK_PROJECT_ID, "-200");
             values.put(Tasks.TASK_SPRINT_ID, "-400");
             values.put(Tasks.TASK_SUMMARY, "Definir modelo de datos");
             values.put(Tasks.TASK_DESCRIPTION, "Se define el modelo de datos de la app.");
             values.put(Tasks.TASK_ESTIMATED, "4");
             values.put(Tasks.TASK_BURNED, "4");
             values.put(Tasks.TASK_REMAINING, "0");
             values.put(Tasks.TASK_PRIORITY, "2");
             values.put(Tasks.TASK_BEGINDATE, "2011-12-01");
             values.put(Tasks.TASK_ENDDATE, "2011-12-01");
             values.put(Tasks.TASK_STATUS, "2");
             values.put(Tasks.TASK_CREATED, "2012-03-07");
             values.put(Tasks.TASK_COMMENTS, "comentario");
             cr.insert(Tasks.CONTENT_URI, values);
 
             values.clear();
             values.put(Tasks.TASK_USER_ID, "-101");
             values.put(Tasks.TASK_PROJECT_ID, "-200");
             values.put(Tasks.TASK_SPRINT_ID, "-400");
             values.put(Tasks.TASK_SUMMARY, "Definir casos de uso iniciales");
             values.put(Tasks.TASK_DESCRIPTION, "Se definen los casos de uso.");
             values.put(Tasks.TASK_ESTIMATED, "2");
             values.put(Tasks.TASK_BURNED, "3");
             values.put(Tasks.TASK_REMAINING, "0");
             values.put(Tasks.TASK_PRIORITY, "10");
             values.put(Tasks.TASK_BEGINDATE, "2011-12-01");
             values.put(Tasks.TASK_ENDDATE, "2011-12-01");
             values.put(Tasks.TASK_STATUS, "2");
             values.put(Tasks.TASK_CREATED, "2012-03-07");
             values.put(Tasks.TASK_COMMENTS, "comentario");
             cr.insert(Tasks.CONTENT_URI, values);
 
             values.clear();
             values.put(Tasks.TASK_USER_ID, "-102");
             values.put(Tasks.TASK_PROJECT_ID, "-200");
             values.put(Tasks.TASK_SPRINT_ID, "-400");
             values.put(Tasks.TASK_SUMMARY, "Creacion de estructura del proyecto");
             values.put(Tasks.TASK_DESCRIPTION, "Arquitectura maven y eclipse.");
             values.put(Tasks.TASK_ESTIMATED, "2");
             values.put(Tasks.TASK_BURNED, "2");
             values.put(Tasks.TASK_REMAINING, "0");
             values.put(Tasks.TASK_PRIORITY, "10");
             values.put(Tasks.TASK_BEGINDATE, "2011-12-01");
             values.put(Tasks.TASK_ENDDATE, "2011-12-01");
             values.put(Tasks.TASK_STATUS, "2");
             values.put(Tasks.TASK_CREATED, "2012-03-07");
             values.put(Tasks.TASK_COMMENTS, "comentario");
             cr.insert(Tasks.CONTENT_URI, values);
 
             // Users
             values.clear();
 
             values.put(Users.USER_FULLNAME, "Ignacio Baca");
             values.put(Users.USER_NICKNAME, "ibaca");
             values.put(Users.USER_EMAIL, "ignacio@gmail.com");
             values.put(Users.USER_NUMBER, "957700652");
             values.put(Users.USER_COMPANY, "Inftel");
             values.put(Users.USER_PASS, "inftel");
             values.put(Users.USER_ROLE, "SM");
 
             cr.insert(Users.CONTENT_URI, values);
 
             values.put(Users.USER_FULLNAME, "Juan Ant. Cobo");
             values.put(Users.USER_NICKNAME, "JuaNaN");
             values.put(Users.USER_EMAIL, "juanan20@gmail.com");
             values.put(Users.USER_NUMBER, "957700652");
             values.put(Users.USER_COMPANY, "Inftel");
             values.put(Users.USER_PASS, "inftel");
             values.put(Users.USER_ROLE, "SM");
 
             cr.insert(Users.CONTENT_URI, values);
 
             values.put(Users.USER_FULLNAME, "Jesus Ruiz");
             values.put(Users.USER_NICKNAME, "3");
             values.put(Users.USER_EMAIL, "jrovillano@gmail.com");
             values.put(Users.USER_NUMBER, "957700652");
             values.put(Users.USER_COMPANY, "Master Inftel");
             values.put(Users.USER_PASS, "inftel");
             values.put(Users.USER_ROLE, "SM");
 
             cr.insert(Users.CONTENT_URI, values);
 
             values.put(Users.USER_FULLNAME, "Jesus Barriga");
             values.put(Users.USER_NICKNAME, "4");
             values.put(Users.USER_EMAIL, "jesusbarriga@gmail.com");
             values.put(Users.USER_NUMBER, "957700652");
             values.put(Users.USER_COMPANY, "Master Inftel");
             values.put(Users.USER_PASS, "inftel");
             values.put(Users.USER_ROLE, "SM");
 
             cr.insert(Users.CONTENT_URI, values);
         }
 
     }
 
 }
