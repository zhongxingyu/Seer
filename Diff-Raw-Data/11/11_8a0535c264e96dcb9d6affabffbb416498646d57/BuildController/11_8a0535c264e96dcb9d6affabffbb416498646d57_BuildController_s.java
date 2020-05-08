 /***
  * 
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. Neither the name of the
  * copyright holders nor the names of its contributors may be used to endorse or
  * promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package br.com.caelum.integracao.server.logic;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.server.Build;
 import br.com.caelum.integracao.server.Project;
 import br.com.caelum.integracao.server.Projects;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 
 @Resource
 public class BuildController {
 	
 	
 	private final Logger logger = LoggerFactory.getLogger(BuildController.class);
 	private final Result result;
 	private final Projects projects;
 	
 	public BuildController(Projects projects, Result result) {
 		this.projects = projects;
 		this.result = result;
 	}
 
 
 	@Get
	@Path("/project/{project.name}/build/{buildId}")
 	public void show(Project project, Long buildId, String filename) {
 		logger.debug("Displaying build result for " + project.getName() + "@build-" + buildId + "@" + filename);
 		project = projects.get(project.getName());
 		result.include("project", project);
 		Build build = project.getBuild(buildId);
 		result.include("build", build);
 		if (filename.equals("")) {
 			result.include("currentPath", "");
 			result.include("content", build.getContent());
 		} else {
			filename = filename.replace('$', '/');
 			File base = build.getFile(filename);
			result.include("currentPath", filename + "$");
 			result.include("content", base.listFiles());
 		}
 	}
 
 	@Get
	@Path("/download/project/{project.name}/build/{buildId}")
 	public File showFile(Project project, Long buildId, String filename) {
 		logger.debug("Displaying file for " + project.getName() + "@" + buildId + ", file=" + filename);
 		project = projects.get(project.getName());
 		Build build = project.getBuild(buildId);
 		return build.getFile(filename.replace('$', '/'));
 	}
 
 }
