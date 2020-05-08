 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel, Lukas Gross
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.exporter.test;
 
 import org.jeconfig.api.dto.ComplexConfigDTO;
 import org.jeconfig.api.scope.ScopePath;
 
 public class CountingDummyPersister extends DummyPersister {
 	private int loadCount = 0;
 	private int saveCount = 0;
 	private int updateCount = 0;
 
 	@Override
 	public ComplexConfigDTO loadConfiguration(final ScopePath scope) {
 		loadCount++;
 		return super.loadConfiguration(scope);
 	}
 
 	@Override
 	public void saveConfiguration(final ComplexConfigDTO configuration) {
 		saveCount++;
 		super.saveConfiguration(configuration);
 	}
 
 	@Override
 	public void updateConfiguration(final ComplexConfigDTO configuration) {
 		updateCount++;
 		super.updateConfiguration(configuration);
 	}
 }
