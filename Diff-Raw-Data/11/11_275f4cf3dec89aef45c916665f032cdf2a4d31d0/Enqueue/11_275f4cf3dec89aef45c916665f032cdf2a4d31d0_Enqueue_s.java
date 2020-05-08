 package com.webwarp.hookhub;
 
 import io.iron.ironworker.client.Client;
 import io.iron.ironworker.client.entities.TaskEntity;
 import io.iron.ironworker.client.builders.Params;
 //import io.iron.ironworker.client.builders.TaskOptions;
 import io.iron.ironworker.client.APIException;
 
 public class Enqueue {
 
     public static void main(String[] args) throws APIException {
        Client client = new Client("cd2HG0XE-jXv31hA9mo5pRZOMcA", "50e83541d429795bf8000e30");
         TaskEntity task = client.createTask("Worker101", Params.add("query", "iron"));
         System.out.println(task.getId());
     }
 }
