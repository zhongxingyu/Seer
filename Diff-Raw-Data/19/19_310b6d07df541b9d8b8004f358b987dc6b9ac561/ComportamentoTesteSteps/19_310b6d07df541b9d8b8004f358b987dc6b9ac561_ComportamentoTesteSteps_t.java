 package com.codificando.jbehavebr;
 
 import org.jbehave.scenario.annotations.Given;
 import org.jbehave.scenario.annotations.Then;
 import org.jbehave.scenario.annotations.When;
 
 public class ComportamentoTesteSteps extends PtBRSteps {
     @Given("que eu quero rodar o Jbehave em português do Brasil")
     public void temTamanho() {}
  
     @When("eu usar o meu idioma")
     public void verificaTamanho() {}

    @When("estiver testando")
    public void eEstiverTestando() {}    
    
    @Then("tudo deve funcionar")
    public void tudoDeveFuncionar() {}

    @Then("eu sou feliz")
    public void eSouFeliz() {}
 }
