 
 package org.inftel.ssa.mobile.ui;
 
 import java.util.List;
 
 import org.inftel.ssa.domain.ProjectProxy;
 import org.inftel.ssa.domain.TaskProxy;
 import org.inftel.ssa.domain.UserProxy;
 import org.inftel.ssa.mobile.R;
 import org.inftel.ssa.mobile.utils.Util;
 import org.inftel.ssa.services.SsaRequestFactory;
 import org.inftel.ssa.services.UserRequest;
 
import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
public class SsaActivity extends Activity {
 
     private TextView text;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ssa_activity);
         // save useful elements
         text = (TextView) findViewById(R.id.ssa_activity_text);
 
         findViewById(R.id.ssa_activity_button_projects).setOnClickListener(
                 new View.OnClickListener() {
 
                     @Override
                     public void onClick(View v) {
                         startActivity(new Intent(SsaActivity.this, ProjectActivity.class));
                     }
                 });
         findViewById(R.id.ssa_activity_button_users).setOnClickListener(
                 new View.OnClickListener() {
 
                     @Override
                     public void onClick(View v) {
                         startActivity(new Intent(SsaActivity.this, UserActivity.class));
                     }
                 });
         findViewById(R.id.ssa_activity_button_tasks).setOnClickListener(
                 new View.OnClickListener() {
 
                     @Override
                     public void onClick(View v) {
                         startActivity(new Intent(SsaActivity.this, TaskActivity.class));
                     }
                 });
 
         // testRequestFactory();
 
     }
 
     private void testRequestFactory() {
         SsaRequestFactory rf = Util.getRequestFactory(this, SsaRequestFactory.class);
         UserRequest usersContext = rf.userRequest();
         usersContext.findAllUsers().with("projects", "tasks").fire(new Receiver<List<UserProxy>>() {
             @Override
             public void onSuccess(List<UserProxy> response) {
                 for (UserProxy userProxy : response) {
                     text.append("obtenido user: " + userProxy.getEmail() + "\n");
                     for (ProjectProxy project : userProxy.getProjects()) {
                         text.append("    project: " + project.getName() + "\n");
                     }
                     for (TaskProxy task : userProxy.getTasks()) {
                         text.append("    task: " + task.getSummary() + " \n");
                     }
                 }
             }
 
             @Override
             public void onFailure(ServerFailure error) {
                 text.append("fallo obtieniendo usuarios: " + error.getMessage());
             }
         });
     }
 
 }
