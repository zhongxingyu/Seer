 package com.csg.warrior.raydotcom.service;
 
 import com.csg.warrior.core.WarriorKeyStatus;
import com.csg.warrior.raydotcom.exception.WarriorRequestException;
import com.csg.warrior.raydotcom.exception.WarriorSignUpException;
 import com.csg.warrior.raydotcom.exception.WarriorKeyRequestException;
 
 public interface WarriorService {
     WarriorKeyStatus getWarriorKeyStatus(String username, String requestSourceUrl) throws WarriorRequestException;
 
     boolean isSignUpSuccessFromReply(String response);
     
     void createQuadrupleOnWarrior(String username, String gcm_device_id) throws WarriorKeyRequestException;
 }
