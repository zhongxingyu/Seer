 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.resource.builtin.core;
 
 import com.kloudtek.systyrant.AbstractVagrantTest;
 import com.kloudtek.systyrant.exception.*;
 import com.kloudtek.systyrant.host.SshHost;
import com.kloudtek.systyrant.resource.Resource;
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
    public void testCreateNonExistingFile() throws STRuntimeException, IOException, ScriptException, ResourceCreationException {
        sshHost.exec("rm -rf /root/testCreateNonExistingFile");
        ctx.getResourceManager().createResource("core:file").set("path","/root/testCreateNonExistingFile")
                .set("content","hello").set("owner","uucp").set("group","fuse").set("permissions","rwxr-xrw-");
         execute();
        String stats = sshHost.exec("stat -c '%F:%s:%A:%U:%G' /root/testCreateNonExistingFile");
         Assert.assertEquals(stats, "regular file:5:-rwxr-xrw-:uucp:fuse\n");
     }
 }
