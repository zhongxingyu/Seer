 /**
  * Copyright (c) 2010, MiddleCraft Contributors
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice, this list of 
  *    conditions and the following disclaimer.
  * 
  *  * Redistributions in binary form must reproduce the above copyright notice, this list 
  *    of conditions and the following disclaimer in the documentation and/or other materials 
  *    provided with the distribution.
  * 
  *  * Neither the name of MiddleCraft nor the names of its contributors may be 
  *    used to endorse or promote products derived from this software without specific prior 
  *    written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javassist.ClassPool;
 import javassist.Loader;
 import javassist.NotFoundException;
import javassist.util.HotSwapper;
 
 import org.middlecraft.server.Renamer;
 import org.middlecraft.server.SmartReflector;
 
 public class Main {
 	static Logger l = Logger.getLogger("Minecraft");
 	public static ClassPool classPool;
 	public static Loader loader;
 	//public static HotSwapper hotswap = new HotSwapper(8000);
 	public static void main(String[] arguments) {
 		
 		// Load mappings.
 		try {
 			SmartReflector.initialize();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Load up plugins. *guitar riff*
 		Hooks.initialize();
 		
 		// Start up server. *drumroll*
 		Renamer r = new Renamer();
 		classPool = ClassPool.getDefault();
 		classPool.appendSystemPath();
 		try {
 			classPool.appendClassPath("lib/*");
 		} catch (NotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		loader = new Loader();
 		try {
 			loader.addTranslator(classPool, r);
 			loader.run("net.minecraft.server.MinecraftServer", arguments);
 		} catch (Throwable e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		SmartReflector.save();
 	}
 }
