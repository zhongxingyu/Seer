 package com.codificando.jbehavebr;
 
 import org.jbehave.scenario.annotations.Given;
 import org.jbehave.scenario.annotations.Then;
 import org.jbehave.scenario.annotations.When;
 
 public class ComportamentoTesteSteps extends PtBRSteps {
     @Given("que eu quero rodar o Jbehave em português do Brasil")
     public void temTamanho() {}
  
     @When("eu usar o meu idioma")
     public void verificaTamanho() {}
 
    @Then("tudo deve funcionar perfeitamente")
    public void erroUploadContentType() {}
 }
