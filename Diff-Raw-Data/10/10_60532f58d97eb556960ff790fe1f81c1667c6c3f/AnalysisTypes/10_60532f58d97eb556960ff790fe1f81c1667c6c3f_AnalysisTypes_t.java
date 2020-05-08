 /*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Copyright (C) 2013 Politecnico di Torino, Italy
                    TORSEC group -- http://security.polito.it
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package gov.niarl.hisAppraiser.hibernate.domain;
 
 
 public class AnalysisTypes {
 
 	private Long id;
 
 	private String name;
 
 	private String module;
 
 	private Integer version;
 
 	private String URL;
 
 	private	boolean deleted;
 
	private String requiredPcrMask;

 	public AnalysisTypes() {
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public Long getId() {
 		return id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 
 	public void setModule(String module) {
 		this.module = module;
 	}
 	public String getModule() {
 		return module;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setURL(String URL) {
 		this.URL = URL;
 	}
 	public String getURL() {
 		return URL;
 	}
 
 	public void setDeleted(boolean deleted) {
 		this.deleted = deleted;
 	}
 	public boolean getDeleted() {
 		return deleted;
 	}

	public void setRequiredPcrMask(String requiredPcrMask) {
		this.requiredPcrMask = requiredPcrMask;
	}
	public String getRequiredPcrMask() {
		return requiredPcrMask;
	}
 }
