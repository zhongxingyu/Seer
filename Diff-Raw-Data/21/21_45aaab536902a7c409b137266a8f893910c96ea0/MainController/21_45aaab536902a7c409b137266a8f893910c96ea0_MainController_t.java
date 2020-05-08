 package org.mytestlab.controller;
 
 import java.util.Set;
 
 import org.mytestlab.domain.ConditionNode;
 import org.mytestlab.domain.FlowGraph;
 import org.mytestlab.domain.GenericNode;
 import org.mytestlab.domain.Transition;
 import org.mytestlab.dto.GenericNodeDto;
 import org.mytestlab.dto.GenericNodeListDto;
 import org.mytestlab.dto.GenericNodeMapper;
 import org.mytestlab.service.FlowgraphService;
 import org.mytestlab.service.GenericNodeService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/main")
 public class MainController {
 
 	@Autowired
 	private GenericNodeService service;
 	
 	@Autowired
 	private FlowgraphService flowGraphService;
 	
 	@RequestMapping
 	public String getMainPage() {
 		return "main";
 	}
 	
 	@RequestMapping(value="/records")
 	public @ResponseBody GenericNodeListDto getNodes() {
 		
 		GenericNodeListDto nodeListDto = new GenericNodeListDto();
 		nodeListDto.setNodes(GenericNodeMapper.map(service.readAll()));
 		return nodeListDto;
 	}
 	
 	@RequestMapping(value="/get")
 	public @ResponseBody GenericNode get(@RequestBody GenericNode node) {
 		return service.read(node);
 	}
 
 	static int tmpName = 1;
 	@RequestMapping(value="/create", method=RequestMethod.POST)
 	public @ResponseBody Set<String> create(
			@RequestParam String codestr) {
 
		System.out.println("In create()");
		
 		FlowGraph fg = new FlowGraph();
 		
		//fg.setName(Integer.toString(tmpName));
		//tmpName++;
		
		fg.setName(Long.toString(System.currentTimeMillis()));
 		
		String[] strs = codestr.split("\n");
 		for(int i = 0; i < strs.length; i++) {
 			fg.addCodeString(strs[i]);
 		}
 		
 		flowGraphService.create(fg);
 		
 		FlowGraph fg2 = flowGraphService.read(fg.getName());
		
		
		System.out.println("Out create()");
		
		
 		if (fg2 != null) {
 			return fg2.getCodeStrings();
 		} else {
 			return null;
 		}
 	}
 	
 	/*@RequestMapping(value="/create", method=RequestMethod.POST)
 	public @ResponseBody GenericNodeDto create(
 			@RequestParam String name,
 			@RequestParam String description,
 			@RequestParam Integer type) {
 
 		GenericNode newNode;
 		Transition newTransition = new Transition("tmpTransition");
 		
 		if(type==1) {
 			newNode = new ConditionNode(name, description);
 			
 			System.out.println("Create condition node");
 			
 		} else {
 			newNode = new GenericNode(name, description);
 		}
 		
 		newNode.setOutgoingRelationship(newTransition);
 		
 		
 		//System.out.println("Add incoming relationship");
 		
 		//Transition newTransition2 = new Transition("test2"+name);	
 		//newNode.addIncomingRelationship(newTransition2);
 		
 		//System.out.println("Added incoming relationship");
 		
 		//newNode.setTest("test"+description);
 		
 		
 		System.out.println("Create node in service");
 		
 		
 		return GenericNodeMapper.map(service.create(newNode));
 	}*/		
 	
 	@RequestMapping(value="/update", method=RequestMethod.POST)
 	public @ResponseBody GenericNodeDto update(
 			@RequestParam String name,
 			@RequestParam String description,
 			@RequestParam String transitionName) {
 
 		Transition newTransition = new Transition(transitionName);
 		
 		GenericNode existingNode = new GenericNode(name, description);
 		existingNode.setOutgoingRelationship(newTransition);
 		
 		return GenericNodeMapper.map(service.update(existingNode));
 	}
 	
 	@RequestMapping(value="/delete", method=RequestMethod.POST)
 	public @ResponseBody Boolean delete(
 			@RequestParam String name) {
 
 		GenericNode existingNode = new GenericNode();
 		existingNode.setName(name);
 		
 		return service.delete(existingNode);
 	}
 	
 }
