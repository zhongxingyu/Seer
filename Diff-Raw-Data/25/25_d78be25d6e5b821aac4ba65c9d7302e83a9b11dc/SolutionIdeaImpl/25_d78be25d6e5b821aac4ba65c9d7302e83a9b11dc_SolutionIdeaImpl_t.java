 package com.tdm.server.logic.model;
 
 import java.util.Date;
 
 import com.tdm.common.dto.GdmProblemId;
 import com.tdm.common.dto.SolutionIdea;
 import com.tdm.common.dto.SolutionIdeaId;
 
 public class SolutionIdeaImpl implements SolutionIdea {
 
 	private SolutionIdeaId id;
 	private GdmProblemId problemId;
 	private String name;
 	private String details;
 	private Date creation;
 
	public SolutionIdeaImpl(int i, int j, String string) {
		// TODO Auto-generated constructor stub
	}

 	@Override
 	public SolutionIdeaId getId() {
 		return id;
 	}
 
 	@Override
 	public GdmProblemId getProblemId() {
 		return problemId;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getDetails() {
 		return details;
 	}
 
 	@Override
 	public Date getCreation() {
 		return creation;
 	}
 
 	public void setId(SolutionIdeaId id) {
 		this.id = id;
 	}
 
 	public void setProblemId(GdmProblemId problemId) {
 		this.problemId = problemId;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
