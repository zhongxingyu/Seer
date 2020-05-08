 package org.hazelwire.test;
 
import org.hazelwire.main.Configuration;
import org.hazelwire.virtualmachine.VMHandler;
 
 /** This example demonstrates uploading of a file over SCP to the SSH server. */
 public class MainTest {
 
     public static void main(String[] args) throws Exception
     {
    	VMHandler vmHandler = new VMHandler("/usr/bin/vboxmanage", "HazelwireTest", "test", true);
    	vmHandler.addForward("ssh", 2222, 22, "TCP");
     }
 }
