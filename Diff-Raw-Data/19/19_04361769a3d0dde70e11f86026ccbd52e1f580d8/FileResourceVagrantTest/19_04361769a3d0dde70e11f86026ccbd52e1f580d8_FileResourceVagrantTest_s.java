 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.resource.builtin.core;
 
 import com.kloudtek.systyrant.AbstractVagrantTest;
 import com.kloudtek.systyrant.exception.*;
 import com.kloudtek.systyrant.host.SshHost;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import javax.script.ScriptException;
 import java.io.IOException;
 
 public class FileResourceVagrantTest extends AbstractVagrantTest {
     @BeforeClass
     public void init() throws ResourceCreationException, InvalidResourceDefinitionException, IOException, STRuntimeException {
         super.init();
     }
 
     @Test(groups = "vagrant")
    public void testCreateNonExistingFile() throws STRuntimeException, IOException, ScriptException {
        sshHost.exec("rm -f /root/testfile");
        ctx.runDSLScript("new core:file { path='/root/testfile', content='hello', owner='uucp', group='fuse' permissions='rwxr-xrw-'}");
         execute();
        String stats = sshHost.exec("stat -c '%F:%s:%A:%U:%G' /root/testfile");
         Assert.assertEquals(stats, "regular file:5:-rwxr-xrw-:uucp:fuse\n");
     }
 }
