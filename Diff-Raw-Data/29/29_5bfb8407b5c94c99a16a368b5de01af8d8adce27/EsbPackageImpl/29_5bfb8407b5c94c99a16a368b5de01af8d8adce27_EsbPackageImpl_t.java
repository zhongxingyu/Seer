 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.wso2.developerstudio.eclipse.gmf.esb.impl;
 
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EGenericType;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResource;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceEndpoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceEndpointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceEndpointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceFaultInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.APIResourceOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractBooleanFeature;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractCommonTarget;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractLocationKeyResource;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractNameValueExpressionProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractNameValueProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.AbstractSqlExecutorMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.AdditionalOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AddressEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.AddressEndPointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AddressEndPointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AggregateMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.AggregateMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AggregateMediatorOnCompleteOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AggregateMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.AggregateSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ApiResourceUrlStyle;
 import org.wso2.developerstudio.eclipse.gmf.esb.ArtifactType;
 import org.wso2.developerstudio.eclipse.gmf.esb.AutoscaleInMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.AutoscaleOutMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.BuilderMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.BuilderMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.BuilderMediatorOutputConector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheImplementationType;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheMediatorOnHitOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheOnHitBranch;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheScope;
 import org.wso2.developerstudio.eclipse.gmf.esb.CacheSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.CallTemplateMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.CallTemplateMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CallTemplateMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CallTemplateParameter;
 import org.wso2.developerstudio.eclipse.gmf.esb.CalloutMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.CalloutMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CalloutMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CalloutPayloadType;
 import org.wso2.developerstudio.eclipse.gmf.esb.CalloutResultType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ClassMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.ClassMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ClassMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ClassProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneMediatorContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneMediatorTargetOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneTargetContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.CloneTarget;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandPropertyContextAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandPropertyMessageAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.CommandPropertyValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ConditionalRouteBranch;
 import org.wso2.developerstudio.eclipse.gmf.esb.ConditionalRouterMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.ConditionalRouterMediatorAdditionalOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ConditionalRouterMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ConditionalRouterMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBLookupMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBLookupMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBLookupMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBReportMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBReportMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DBReportMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DefaultEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.DefaultEndPointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DefaultEndPointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.DropMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.DropMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndPointAddressingVersion;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndPointAttachmentOptimization;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndPointMessageFormat;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndPointTimeOutAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndpointDiagram;
 import org.wso2.developerstudio.eclipse.gmf.esb.EndpointFlow;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnqueueMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnqueueMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnqueueMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichSourceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichTargetAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.EnrichTargetType;
 import org.wso2.developerstudio.eclipse.gmf.esb.EntitlementMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.EntitlementMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EntitlementMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbDiagram;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbElement;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbFactory;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbLink;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbNode;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbPackage;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbSequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbSequenceInput;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbSequenceInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbSequenceOutput;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbSequenceOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbServer;
 import org.wso2.developerstudio.eclipse.gmf.esb.EvaluatorExpressionProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.EventMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.EventMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EventMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.EventTopicType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ExpressionAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPointCaseBranchOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPointDefaultBranchOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FailoverEndPointWestOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultCodeSoap11;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultCodeSoap12;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultDetailType;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultReasonType;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultSoapVersion;
 import org.wso2.developerstudio.eclipse.gmf.esb.FaultStringType;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterConditionType;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterFailContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterMediatorFailOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterMediatorPassOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.FilterPassContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.HeaderAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.HeaderMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.HeaderMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.HeaderMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.HeaderValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.InputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.IterateMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.IterateMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.IterateMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.IterateMediatorTargetOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.IterateTarget;
 import org.wso2.developerstudio.eclipse.gmf.esb.KeyType;
 import org.wso2.developerstudio.eclipse.gmf.esb.LoadBalanceEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.LoadBalanceEndPointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.LoadBalanceEndPointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.LoadBalanceEndPointWestOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.LocalEntry;
 import org.wso2.developerstudio.eclipse.gmf.esb.LocalEntryValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogCategory;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogLevel;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.LogProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.Mediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.MediatorFlow;
 import org.wso2.developerstudio.eclipse.gmf.esb.MediatorSequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.MergeNode;
 import org.wso2.developerstudio.eclipse.gmf.esb.MergeNodeFirstInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.MergeNodeOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.MergeNodeSecondInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.MessageBuilder;
 import org.wso2.developerstudio.eclipse.gmf.esb.MessageInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.MessageMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.MessageOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.NameValueTypeProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.NamedEndpoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.NamedEndpointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.NamedEndpointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.NamespacedProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.OAuthMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.OAuthMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.OAuthMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.OutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.OutputMethod;
 import org.wso2.developerstudio.eclipse.gmf.esb.ParentEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.PayloadFactoryArgument;
 import org.wso2.developerstudio.eclipse.gmf.esb.PayloadFactoryArgumentType;
 import org.wso2.developerstudio.eclipse.gmf.esb.PayloadFactoryMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.PayloadFactoryMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.PayloadFactoryMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyDataType;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyScope;
 import org.wso2.developerstudio.eclipse.gmf.esb.PropertyValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyFaultInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyService;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceEndpointContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceFaultContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceInSequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceOutSequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceParameter;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServicePolicy;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceSequenceAndEndpointContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyServiceSequenceContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ProxyWsdlType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RMSequenceMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.RMSequenceMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RMSequenceMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RMSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RMSpecVersion;
 import org.wso2.developerstudio.eclipse.gmf.esb.ReceivingSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RegistryKeyProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouteTarget;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterMediatorContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterMediatorTargetOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterRoute;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterTarget;
 import org.wso2.developerstudio.eclipse.gmf.esb.RouterTargetContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleActionType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleActions;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleChildMediatorsConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleFact;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleFactType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleFactValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleFactsConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleFragmentType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleMediatorChildMediatorsOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleOptionType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleResult;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleResultType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleResultValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleResultsConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleSessionProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleSetCreationProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.RuleSourceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ScriptLanguage;
 import org.wso2.developerstudio.eclipse.gmf.esb.ScriptMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.ScriptMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ScriptMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ScriptType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SendContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.SendMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.SendMediatorEndpointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SendMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SendMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.Sequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequenceDiagram;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequenceInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequenceOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.Sequences;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequencesInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SequencesOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.Session;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksIODataType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksInConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksOutConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.SomeXML;
 import org.wso2.developerstudio.eclipse.gmf.esb.SpringMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.SpringMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SpringMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlExecutorBooleanValue;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlExecutorConnectionType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlExecutorDatasourceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlExecutorIsolationLevel;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlParameterDataType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlParameterDefinition;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlParameterValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlResultMapping;
 import org.wso2.developerstudio.eclipse.gmf.esb.SqlStatement;
 import org.wso2.developerstudio.eclipse.gmf.esb.StoreMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.StoreMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.StoreMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchCaseBranchOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchCaseContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchDefaultBranchOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchDefaultContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchMediatorContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SwitchMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.SynapseAPI;
 import org.wso2.developerstudio.eclipse.gmf.esb.TargetEndpointType;
 import org.wso2.developerstudio.eclipse.gmf.esb.TargetSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.Template;
 import org.wso2.developerstudio.eclipse.gmf.esb.TemplateType;
 import org.wso2.developerstudio.eclipse.gmf.esb.Task;
 import org.wso2.developerstudio.eclipse.gmf.esb.TaskImplementation;
 import org.wso2.developerstudio.eclipse.gmf.esb.TaskProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.TaskPropertyType;
 import org.wso2.developerstudio.eclipse.gmf.esb.TaskTriggerType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleAccessType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleConditionType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleMediatorOnAcceptOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleMediatorOnRejectOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleOnAcceptBranch;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleOnAcceptContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleOnRejectBranch;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleOnRejectContainer;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottlePolicyConfiguration;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottlePolicyEntry;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottlePolicyType;
 import org.wso2.developerstudio.eclipse.gmf.esb.ThrottleSequenceType;
 import org.wso2.developerstudio.eclipse.gmf.esb.TransactionAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.TransactionMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.TransactionMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.TransactionMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.Trigger;
 import org.wso2.developerstudio.eclipse.gmf.esb.Type;
 import org.wso2.developerstudio.eclipse.gmf.esb.URLRewriteMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.URLRewriteMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.URLRewriteMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.URLRewriteRule;
 import org.wso2.developerstudio.eclipse.gmf.esb.URLRewriteRuleAction;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateFeature;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateMediatorOnFailOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateOnFailBranch;
 import org.wso2.developerstudio.eclipse.gmf.esb.ValidateSchema;
 import org.wso2.developerstudio.eclipse.gmf.esb.WSDLDefinition;
 import org.wso2.developerstudio.eclipse.gmf.esb.WSDLDescription;
 import org.wso2.developerstudio.eclipse.gmf.esb.WSDLEndPoint;
 import org.wso2.developerstudio.eclipse.gmf.esb.WSDLEndPointInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.WSDLEndPointOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryVariable;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryVariableType;
 import org.wso2.developerstudio.eclipse.gmf.esb.XQueryVariableValueType;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTFeature;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTMediatorInputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTMediatorOutputConnector;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.XSLTResource;
 import org.wso2.developerstudio.eclipse.gmf.esb.propertyTaskString;
 import org.wso2.developerstudio.eclipse.gmf.esb.propertyTaskXML;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class EsbPackageImpl extends EPackageImpl implements EsbPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbDiagramEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbElementEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbServerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass inputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass outputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass additionalOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbLinkEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass endPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServiceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyFaultInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServiceParameterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServicePolicyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServiceSequenceAndEndpointContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServiceFaultContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass proxyServiceContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mediatorFlowEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass endpointFlowEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass messageMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass messageInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass messageOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass defaultEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass defaultEndPointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass defaultEndPointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass addressEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass addressEndPointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass addressEndPointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dropMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dropMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass filterMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass filterContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass filterPassContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass filterFailContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass filterMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass filterMediatorOutputConnectorEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass filterMediatorPassOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass filterMediatorFailOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mergeNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mergeNodeFirstInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mergeNodeSecondInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mergeNodeOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass logMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass logMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass logMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass logPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass registryKeyPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass propertyMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass propertyMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass propertyMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass namespacedPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enrichMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enrichMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enrichMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractNameValueExpressionPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractBooleanFeatureEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractLocationKeyResourceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltFeatureEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltResourceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xsltMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchCaseBranchOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchDefaultBranchOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchMediatorContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchCaseContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchDefaultContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequenceDiagramEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbSequenceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbSequenceInputEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbSequenceOutputEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbSequenceInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass esbSequenceOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequenceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequenceInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequenceOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass eventMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass eventMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass eventMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractNameValuePropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass entitlementMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass entitlementMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass entitlementMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enqueueMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enqueueMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass enqueueMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass classMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass classMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass classMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass classPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass springMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass springMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass springMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateFeatureEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateSchemaEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass validateMediatorOnFailOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass endpointDiagramEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass namedEndpointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass namedEndpointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass namedEndpointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass templateEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass taskEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass nameValueTypePropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass taskPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass synapseAPIEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceFaultInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceEndpointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceEndpointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass apiResourceEndpointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum artifactTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass scriptMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass scriptMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass scriptMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass faultMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass faultMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass faultMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass aggregateMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass aggregateMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass aggregateMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass aggregateMediatorOnCompleteOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerRouteEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerMediatorTargetOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerMediatorContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass routerTargetContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneMediatorTargetOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneMediatorContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cloneTargetContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass iterateMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass iterateMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass iterateMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass iterateMediatorTargetOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass iterateTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractCommonTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass mediatorSequenceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cacheMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cacheMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cacheMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cacheMediatorOnHitOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass cacheOnHitBranchEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xQueryMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xQueryMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xQueryMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xQueryVariableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass calloutMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass calloutMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass calloutMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass rmSequenceMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass rmSequenceMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass rmSequenceMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass transactionMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass transactionMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass transactionMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass oAuthMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass oAuthMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass oAuthMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass autoscaleInMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass autoscaleOutMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass headerMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass headerMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass headerMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttleMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttleMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttleMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass throttleMediatorOnAcceptOutputConnectorEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass throttleMediatorOnRejectOutputConnectorEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttlePolicyConfigurationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttlePolicyEntryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttleOnAcceptBranchEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass throttleOnRejectBranchEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass throttleContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass throttleOnAcceptContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass throttleOnRejectContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass commandMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass commandMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass commandMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass commandPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractSqlExecutorMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sqlStatementEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sqlParameterDefinitionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sqlResultMappingEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbLookupMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbLookupMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbLookupMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbReportMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbReportMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass dbReportMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleMediatorChildMediatorsOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleSetCreationPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleSessionPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleFactsConfigurationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleFactEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleResultsConfigurationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleResultEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleChildMediatorsConfigurationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass callTemplateParameterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass callTemplateMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass callTemplateMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass callTemplateMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass smooksMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass smooksMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass smooksMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass storeMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass storeMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass storeMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass builderMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass builderMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass builderMediatorOutputConectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass messageBuilderEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass payloadFactoryMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass payloadFactoryMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass payloadFactoryMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass payloadFactoryArgumentEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionalRouteBranchEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionalRouterMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionalRouterMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionalRouterMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionalRouterMediatorAdditionalOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sendMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass sendContainerEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sendMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sendMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     private EClass sendMediatorEndpointOutputConnectorEClass = null;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass failoverEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass failoverEndPointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass failoverEndPointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass failoverEndPointWestOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass parentEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass wsdlEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass wsdlDefinitionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass wsdlDescriptionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass wsdlEndPointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass wsdlEndPointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass loadBalanceEndPointEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass loadBalanceEndPointInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass loadBalanceEndPointOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass loadBalanceEndPointWestOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass localEntryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sessionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequencesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequencesOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sequencesInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass urlRewriteRuleActionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass urlRewriteRuleEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass urlRewriteMediatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass urlRewriteMediatorInputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass urlRewriteMediatorOutputConnectorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass evaluatorExpressionPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum proxyWsdlTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum filterConditionTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum logCategoryEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum logLevelEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum endPointAddressingVersionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum endPointTimeOutActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum endPointMessageFormatEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum endPointAttachmentOptimizationEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum propertyDataTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum propertyActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum propertyScopeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum propertyValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum enrichSourceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum enrichTargetActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum enrichTargetTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum eventTopicTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum scriptTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum scriptLanguageEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultSoapVersionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultCodeSoap11EEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultCodeSoap12EEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultStringTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultReasonTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum faultDetailTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum aggregateSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum targetSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum targetEndpointTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum cacheSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum cacheImplementationTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum cacheActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum cacheScopeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum xQueryVariableTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum xQueryVariableValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum calloutPayloadTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum calloutResultTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum rmSpecVersionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum rmSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum transactionActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum headerActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum headerValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum throttlePolicyTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum throttleConditionTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum throttleAccessTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum throttleSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum commandPropertyValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum commandPropertyMessageActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum commandPropertyContextActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlExecutorConnectionTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlExecutorDatasourceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlExecutorBooleanValueEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlExecutorIsolationLevelEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlParameterValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum sqlParameterDataTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleActionsEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleSourceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleFactTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleFactValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleResultTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleResultValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleOptionTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum smooksIODataTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum expressionActionEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum outputMethodEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum receivingSequenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum keyTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum payloadFactoryArgumentTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum typeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum localEntryValueTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleActionTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum ruleFragmentTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum templateTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum taskPropertyTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum taskTriggerTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum apiResourceUrlStyleEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType mapEDataType = null;
 
 	/**
 	 * Creates an instance of the model <b>Package</b>, registered with
 	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
 	 * package URI value.
 	 * <p>Note: the correct way to create the package is via the static
 	 * factory method {@link #init init()}, which also performs
 	 * initialization of the package, or returns the registered package,
 	 * if one already exists.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.ecore.EPackage.Registry
 	 * @see org.wso2.developerstudio.eclipse.gmf.esb.EsbPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private EsbPackageImpl() {
 		super(eNS_URI, EsbFactory.eINSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static boolean isInited = false;
 
 	/**
 	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
 	 * 
 	 * <p>This method is used to initialize {@link EsbPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static EsbPackage init() {
 		if (isInited) return (EsbPackage)EPackage.Registry.INSTANCE.getEPackage(EsbPackage.eNS_URI);
 
 		// Obtain or create and register package
 		EsbPackageImpl theEsbPackage = (EsbPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof EsbPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new EsbPackageImpl());
 
 		isInited = true;
 
 		// Create package meta-data objects
 		theEsbPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theEsbPackage.initializePackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theEsbPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(EsbPackage.eNS_URI, theEsbPackage);
 		return theEsbPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbDiagram() {
 		return esbDiagramEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbDiagram_Server() {
 		return (EReference)esbDiagramEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEsbDiagram_Test() {
 		return (EAttribute)esbDiagramEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbNode() {
 		return esbNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbElement() {
 		return esbElementEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbServer() {
 		return esbServerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbServer_Children() {
 		return (EReference)esbServerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbServer_MessageMediator() {
 		return (EReference)esbServerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEsbServer_Type() {
 		return (EAttribute)esbServerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMediator() {
 		return mediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMediator_Reverse() {
 		return (EAttribute)mediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbConnector() {
 		return esbConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getInputConnector() {
 		return inputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getInputConnector_IncomingLinks() {
 		return (EReference)inputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOutputConnector() {
 		return outputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getOutputConnector_OutgoingLink() {
 		return (EReference)outputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAdditionalOutputConnector() {
 		return additionalOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAdditionalOutputConnector_AdditionalOutgoingLink() {
 		return (EReference)additionalOutputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbLink() {
 		return esbLinkEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbLink_Source() {
 		return (EReference)esbLinkEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbLink_Target() {
 		return (EReference)esbLinkEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEndPoint() {
 		return endPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEndPoint_EndPointName() {
 		return (EAttribute)endPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEndPoint_Anonymous() {
 		return (EAttribute)endPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyService() {
 		return proxyServiceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_OutputConnector() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_InputConnector() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_FaultInputConnector() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_Name() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_PinnedServers() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_ServiceGroup() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_TraceEnabled() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_StatisticsEnabled() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_Transports() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_ReliableMessagingEnabled() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_SecurityEnabled() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_WsdlType() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_WsdlXML() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_WsdlURL() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_WsdlKey() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_ServiceParameters() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_ServicePolicies() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_Container() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_InSequenceType() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_InSequenceKey() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_InSequenceName() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_OutSequenceType() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_OutSequenceKey() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_OutSequenceName() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_FaultSequenceType() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyService_FaultSequenceKey() {
 		return (EReference)proxyServiceEClass.getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyService_FaultSequenceName() {
 		return (EAttribute)proxyServiceEClass.getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyOutputConnector() {
 		return proxyOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyInputConnector() {
 		return proxyInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyFaultInputConnector() {
 		return proxyFaultInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyServiceParameter() {
 		return proxyServiceParameterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyServiceParameter_Name() {
 		return (EAttribute)proxyServiceParameterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProxyServiceParameter_Value() {
 		return (EAttribute)proxyServiceParameterEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyServicePolicy() {
 		return proxyServicePolicyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyServicePolicy_PolicyKey() {
 		return (EReference)proxyServicePolicyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyServiceSequenceAndEndpointContainer() {
 		return proxyServiceSequenceAndEndpointContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyServiceSequenceAndEndpointContainer_MediatorFlow() {
 		return (EReference)proxyServiceSequenceAndEndpointContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyServiceFaultContainer() {
 		return proxyServiceFaultContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyServiceFaultContainer_MediatorFlow() {
 		return (EReference)proxyServiceFaultContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProxyServiceContainer() {
 		return proxyServiceContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyServiceContainer_SequenceAndEndpointContainer() {
 		return (EReference)proxyServiceContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProxyServiceContainer_FaultContainer() {
 		return (EReference)proxyServiceContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMediatorFlow() {
 		return mediatorFlowEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMediatorFlow_Children() {
 		return (EReference)mediatorFlowEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getEndpointFlow() {
 		return endpointFlowEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getEndpointFlow_Children() {
 		return (EReference)endpointFlowEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractEndPoint() {
 		return abstractEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_ReliableMessagingEnabled() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_SecurityEnabled() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_AddressingEnabled() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_AddressingVersion() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_AddressingSeparateListener() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_TimeOutDuration() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_TimeOutAction() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_RetryErrorCodes() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_RetryCount() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_RetryDelay() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_SuspendErrorCodes() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_SuspendInitialDuration() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_SuspendMaximumDuration() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractEndPoint_SuspendProgressionFactor() {
 		return (EAttribute)abstractEndPointEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractEndPoint_ReliableMessagingPolicy() {
 		return (EReference)abstractEndPointEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractEndPoint_SecurityPolicy() {
 		return (EReference)abstractEndPointEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMessageMediator() {
 		return messageMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMessageMediator_InputConnector() {
 		return (EReference)messageMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMessageMediator_OutputConnector() {
 		return (EReference)messageMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMessageInputConnector() {
 		return messageInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMessageOutputConnector() {
 		return messageOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDefaultEndPoint() {
 		return defaultEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDefaultEndPoint_InputConnector() {
 		return (EReference)defaultEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDefaultEndPoint_OutputConnector() {
 		return (EReference)defaultEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDefaultEndPointInputConnector() {
 		return defaultEndPointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDefaultEndPointOutputConnector() {
 		return defaultEndPointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAddressEndPoint() {
 		return addressEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAddressEndPoint_InputConnector() {
 		return (EReference)addressEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAddressEndPoint_OutputConnector() {
 		return (EReference)addressEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAddressEndPoint_URI() {
 		return (EAttribute)addressEndPointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAddressEndPointInputConnector() {
 		return addressEndPointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAddressEndPointOutputConnector() {
 		return addressEndPointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDropMediator() {
 		return dropMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDropMediator_InputConnector() {
 		return (EReference)dropMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDropMediatorInputConnector() {
 		return dropMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFilterMediator() {
 		return filterMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFilterMediator_ConditionType() {
 		return (EAttribute)filterMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFilterMediator_Regex() {
 		return (EAttribute)filterMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFilterMediator_InputConnector() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterMediator_OutputConnector() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFilterMediator_PassOutputConnector() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFilterMediator_FailOutputConnector() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFilterMediator_Xpath() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFilterMediator_Source() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterMediator_FilterContainer() {
 		return (EReference)filterMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getFilterContainer() {
 		return filterContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterContainer_PassContainer() {
 		return (EReference)filterContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterContainer_FailContainer() {
 		return (EReference)filterContainerEClass.getEStructuralFeatures().get(1);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getFilterPassContainer() {
 		return filterPassContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterPassContainer_MediatorFlow() {
 		return (EReference)filterPassContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getFilterFailContainer() {
 		return filterFailContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getFilterFailContainer_MediatorFlow() {
 		return (EReference)filterFailContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFilterMediatorInputConnector() {
 		return filterMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getFilterMediatorOutputConnector() {
 		return filterMediatorOutputConnectorEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFilterMediatorPassOutputConnector() {
 		return filterMediatorPassOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFilterMediatorFailOutputConnector() {
 		return filterMediatorFailOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMergeNode() {
 		return mergeNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMergeNode_FirstInputConnector() {
 		return (EReference)mergeNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMergeNode_SecondInputConnector() {
 		return (EReference)mergeNodeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMergeNode_OutputConnector() {
 		return (EReference)mergeNodeEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMergeNodeFirstInputConnector() {
 		return mergeNodeFirstInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMergeNodeSecondInputConnector() {
 		return mergeNodeSecondInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMergeNodeOutputConnector() {
 		return mergeNodeOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLogMediator() {
 		return logMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLogMediator_LogCategory() {
 		return (EAttribute)logMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLogMediator_LogLevel() {
 		return (EAttribute)logMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLogMediator_LogSeparator() {
 		return (EAttribute)logMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLogMediator_InputConnector() {
 		return (EReference)logMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLogMediator_OutputConnector() {
 		return (EReference)logMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLogMediator_Properties() {
 		return (EReference)logMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLogMediatorInputConnector() {
 		return logMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLogMediatorOutputConnector() {
 		return logMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLogProperty() {
 		return logPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRegistryKeyProperty() {
 		return registryKeyPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRegistryKeyProperty_PrettyName() {
 		return (EAttribute)registryKeyPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRegistryKeyProperty_KeyName() {
 		return (EAttribute)registryKeyPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRegistryKeyProperty_KeyValue() {
 		return (EAttribute)registryKeyPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRegistryKeyProperty_Filters() {
 		return (EAttribute)registryKeyPropertyEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPropertyMediator() {
 		return propertyMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPropertyMediator_InputConnector() {
 		return (EReference)propertyMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPropertyMediator_OutputConnector() {
 		return (EReference)propertyMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_PropertyName() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_PropertyDataType() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_PropertyAction() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_PropertyScope() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_ValueType() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_ValueLiteral() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_Expression() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_NamespacePrefix() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_Namespace() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPropertyMediator_ValueExpression() {
 		return (EReference)propertyMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_ValueOM() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_ValueStringPattern() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPropertyMediator_ValueStringCapturingGroup() {
 		return (EAttribute)propertyMediatorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPropertyMediatorInputConnector() {
 		return propertyMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPropertyMediatorOutputConnector() {
 		return propertyMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNamespacedProperty() {
 		return namespacedPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamespacedProperty_PrettyName() {
 		return (EAttribute)namespacedPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamespacedProperty_PropertyName() {
 		return (EAttribute)namespacedPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamespacedProperty_PropertyValue() {
 		return (EAttribute)namespacedPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamespacedProperty_Namespaces() {
 		return (EAttribute)namespacedPropertyEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnrichMediator() {
 		return enrichMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_CloneSource() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_SourceType() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnrichMediator_SourceXpath() {
 		return (EReference)enrichMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_SourceProperty() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_SourceXML() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_TargetAction() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_TargetType() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnrichMediator_TargetXpath() {
 		return (EReference)enrichMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnrichMediator_TargetProperty() {
 		return (EAttribute)enrichMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnrichMediator_InputConnector() {
 		return (EReference)enrichMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnrichMediator_OutputConnector() {
 		return (EReference)enrichMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnrichMediatorInputConnector() {
 		return enrichMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnrichMediatorOutputConnector() {
 		return enrichMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractNameValueExpressionProperty() {
 		return abstractNameValueExpressionPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractNameValueExpressionProperty_PropertyName() {
 		return (EAttribute)abstractNameValueExpressionPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractNameValueExpressionProperty_PropertyValueType() {
 		return (EAttribute)abstractNameValueExpressionPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractNameValueExpressionProperty_PropertyValue() {
 		return (EAttribute)abstractNameValueExpressionPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractNameValueExpressionProperty_PropertyExpression() {
 		return (EReference)abstractNameValueExpressionPropertyEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractBooleanFeature() {
 		return abstractBooleanFeatureEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractBooleanFeature_FeatureName() {
 		return (EAttribute)abstractBooleanFeatureEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractBooleanFeature_FeatureEnabled() {
 		return (EAttribute)abstractBooleanFeatureEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractLocationKeyResource() {
 		return abstractLocationKeyResourceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractLocationKeyResource_Location() {
 		return (EAttribute)abstractLocationKeyResourceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractLocationKeyResource_Key() {
 		return (EReference)abstractLocationKeyResourceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTMediator() {
 		return xsltMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_InputConnector() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_OutputConnector() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXSLTMediator_XsltSchemaKeyType() {
 		return (EAttribute)xsltMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_XsltStaticSchemaKey() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_XsltDynamicSchemaKey() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_XsltKey() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_SourceXPath() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_Properties() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_Features() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXSLTMediator_Resources() {
 		return (EReference)xsltMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTProperty() {
 		return xsltPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTFeature() {
 		return xsltFeatureEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTResource() {
 		return xsltResourceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTMediatorInputConnector() {
 		return xsltMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXSLTMediatorOutputConnector() {
 		return xsltMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchMediator() {
 		return switchMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_SourceXpath() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSwitchMediator_Source() {
 		return (EAttribute)switchMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSwitchMediator_Namespace() {
 		return (EAttribute)switchMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSwitchMediator_NamespacePrefix() {
 		return (EAttribute)switchMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_CaseBranches() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_DefaultBranch() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_InputConnector() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_OutputConnector() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediator_SwitchContainer() {
 		return (EReference)switchMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchCaseBranchOutputConnector() {
 		return switchCaseBranchOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSwitchCaseBranchOutputConnector_CaseRegex() {
 		return (EAttribute)switchCaseBranchOutputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchDefaultBranchOutputConnector() {
 		return switchDefaultBranchOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchMediatorInputConnector() {
 		return switchMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchMediatorOutputConnector() {
 		return switchMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchMediatorContainer() {
 		return switchMediatorContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediatorContainer_SwitchCaseContainer() {
 		return (EReference)switchMediatorContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchMediatorContainer_SwitchDefaultContainer() {
 		return (EReference)switchMediatorContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchCaseContainer() {
 		return switchCaseContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchCaseContainer_MediatorFlow() {
 		return (EReference)switchCaseContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchDefaultContainer() {
 		return switchDefaultContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSwitchDefaultContainer_MediatorFlow() {
 		return (EReference)switchDefaultContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequenceDiagram() {
 		return sequenceDiagramEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequenceDiagram_Sequence() {
 		return (EReference)sequenceDiagramEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbSequence() {
 		return esbSequenceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEsbSequence_Name() {
 		return (EAttribute)esbSequenceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbSequence_Input() {
 		return (EReference)esbSequenceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbSequence_Output() {
 		return (EReference)esbSequenceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbSequence_ChildMediators() {
 		return (EReference)esbSequenceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbSequenceInput() {
 		return esbSequenceInputEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbSequenceInput_Connector() {
 		return (EReference)esbSequenceInputEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbSequenceOutput() {
 		return esbSequenceOutputEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEsbSequenceOutput_Connector() {
 		return (EReference)esbSequenceOutputEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbSequenceInputConnector() {
 		return esbSequenceInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEsbSequenceOutputConnector() {
 		return esbSequenceOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequence() {
 		return sequenceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSequence_Name() {
 		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSequence_Key() {
 		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequence_InputConnector() {
 		return (EReference)sequenceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequence_OutputConnector() {
 		return (EReference)sequenceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequence_IncludedMediators() {
 		return (EReference)sequenceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequenceInputConnector() {
 		return sequenceInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequenceOutputConnector() {
 		return sequenceOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEventMediator() {
 		return eventMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEventMediator_TopicType() {
 		return (EAttribute)eventMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEventMediator_StaticTopic() {
 		return (EAttribute)eventMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEventMediator_DynamicTopic() {
 		return (EReference)eventMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEventMediator_EventExpression() {
 		return (EReference)eventMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEventMediator_InputConnector() {
 		return (EReference)eventMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEventMediator_OutputConnector() {
 		return (EReference)eventMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEventMediatorInputConnector() {
 		return eventMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEventMediatorOutputConnector() {
 		return eventMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractNameValueProperty() {
 		return abstractNameValuePropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractNameValueProperty_PropertyName() {
 		return (EAttribute)abstractNameValuePropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractNameValueProperty_PropertyValue() {
 		return (EAttribute)abstractNameValuePropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEntitlementMediator() {
 		return entitlementMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntitlementMediator_ServerURL() {
 		return (EAttribute)entitlementMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntitlementMediator_Username() {
 		return (EAttribute)entitlementMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntitlementMediator_Password() {
 		return (EAttribute)entitlementMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEntitlementMediator_InputConnector() {
 		return (EReference)entitlementMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEntitlementMediator_OutputConnector() {
 		return (EReference)entitlementMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEntitlementMediatorInputConnector() {
 		return entitlementMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEntitlementMediatorOutputConnector() {
 		return entitlementMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnqueueMediator() {
 		return enqueueMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnqueueMediator_Executor() {
 		return (EAttribute)enqueueMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEnqueueMediator_Priority() {
 		return (EAttribute)enqueueMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnqueueMediator_SequenceKey() {
 		return (EReference)enqueueMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnqueueMediator_InputConnector() {
 		return (EReference)enqueueMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEnqueueMediator_OutputConnector() {
 		return (EReference)enqueueMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnqueueMediatorInputConnector() {
 		return enqueueMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEnqueueMediatorOutputConnector() {
 		return enqueueMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getClassMediator() {
 		return classMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getClassMediator_ClassName() {
 		return (EAttribute)classMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getClassMediator_Properties() {
 		return (EReference)classMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getClassMediator_InputConnector() {
 		return (EReference)classMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getClassMediator_OutputConnector() {
 		return (EReference)classMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getClassMediatorInputConnector() {
 		return classMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getClassMediatorOutputConnector() {
 		return classMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getClassProperty() {
 		return classPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSpringMediator() {
 		return springMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSpringMediator_BeanName() {
 		return (EAttribute)springMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSpringMediator_ConfigurationKey() {
 		return (EReference)springMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSpringMediator_InputConnector() {
 		return (EReference)springMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSpringMediator_OutputConnector() {
 		return (EReference)springMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSpringMediatorInputConnector() {
 		return springMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSpringMediatorOutputConnector() {
 		return springMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateMediator() {
 		return validateMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_SourceXpath() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_Features() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_Schemas() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_InputConnector() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_OutputConnector() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_OnFailOutputConnector() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateMediator_MediatorFlow() {
 		return (EReference)validateMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateFeature() {
 		return validateFeatureEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateSchema() {
 		return validateSchemaEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateSchema_ValidateStaticSchemaKey() {
 		return (EReference)validateSchemaEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateSchema_ValidateDynamicSchemaKey() {
 		return (EReference)validateSchemaEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getValidateSchema_ValidateSchemaKeyType() {
 		return (EAttribute)validateSchemaEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getValidateSchema_SchemaKey() {
 		return (EReference)validateSchemaEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateMediatorInputConnector() {
 		return validateMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateMediatorOutputConnector() {
 		return validateMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValidateMediatorOnFailOutputConnector() {
 		return validateMediatorOnFailOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEndpointDiagram() {
 		return endpointDiagramEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEndpointDiagram_Child() {
 		return (EReference)endpointDiagramEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEndpointDiagram_Name() {
 		return (EAttribute)endpointDiagramEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNamedEndpoint() {
 		return namedEndpointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getNamedEndpoint_InputConnector() {
 		return (EReference)namedEndpointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getNamedEndpoint_OutputConnector() {
 		return (EReference)namedEndpointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamedEndpoint_Name() {
 		return (EAttribute)namedEndpointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNamedEndpointInputConnector() {
 		return namedEndpointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNamedEndpointOutputConnector() {
 		return namedEndpointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTemplate() {
 		return templateEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTemplate_Name() {
 		return (EAttribute)templateEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTemplate_TemplateType() {
 		return (EAttribute)templateEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTemplate_Child() {
 		return (EReference)templateEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTask() {
 		return taskEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_TaskName() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_TaskGroup() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_TriggerType() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_Count() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_Interval() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_Cron() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_PinnedServers() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTask_TaskImplementation() {
 		return (EAttribute)taskEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTask_TaskProperties() {
 		return (EReference)taskEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNameValueTypeProperty() {
 		return nameValueTypePropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNameValueTypeProperty_PropertyName() {
 		return (EAttribute)nameValueTypePropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNameValueTypeProperty_PropertyValue() {
 		return (EAttribute)nameValueTypePropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNameValueTypeProperty_PropertyType() {
 		return (EAttribute)nameValueTypePropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTaskProperty() {
 		return taskPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSynapseAPI() {
 		return synapseAPIEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSynapseAPI_ApiName() {
 		return (EAttribute)synapseAPIEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSynapseAPI_Context() {
 		return (EAttribute)synapseAPIEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSynapseAPI_HostName() {
 		return (EAttribute)synapseAPIEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSynapseAPI_Port() {
 		return (EAttribute)synapseAPIEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSynapseAPI_Resources() {
 		return (EReference)synapseAPIEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResource() {
 		return apiResourceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResource_InputConnector() {
 		return (EReference)apiResourceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResource_OutputConnector() {
 		return (EReference)apiResourceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResource_FaultInputConnector() {
 		return (EReference)apiResourceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_UrlStyle() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_UriTemplate() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_UrlMapping() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_AllowGet() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_AllowPost() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_AllowPut() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_AllowDelete() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAPIResource_AllowOptions() {
 		return (EAttribute)apiResourceEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResource_Container() {
 		return (EReference)apiResourceEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceInputConnector() {
 		return apiResourceInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceOutputConnector() {
 		return apiResourceOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceFaultInputConnector() {
 		return apiResourceFaultInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceEndpoint() {
 		return apiResourceEndpointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResourceEndpoint_InputConnector() {
 		return (EReference)apiResourceEndpointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAPIResourceEndpoint_OutputConnector() {
 		return (EReference)apiResourceEndpointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceEndpointInputConnector() {
 		return apiResourceEndpointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAPIResourceEndpointOutputConnector() {
 		return apiResourceEndpointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getArtifactType() {
 		return artifactTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSequenceType() {
 		return sequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getScriptMediator() {
 		return scriptMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getScriptMediator_ScriptType() {
 		return (EAttribute)scriptMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getScriptMediator_ScriptLanguage() {
 		return (EAttribute)scriptMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getScriptMediator_MediateFunction() {
 		return (EAttribute)scriptMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getScriptMediator_ScriptKey() {
 		return (EReference)scriptMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getScriptMediator_ScriptBody() {
 		return (EAttribute)scriptMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getScriptMediator_InputConnector() {
 		return (EReference)scriptMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getScriptMediator_OutputConnector() {
 		return (EReference)scriptMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getScriptMediatorInputConnector() {
 		return scriptMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getScriptMediatorOutputConnector() {
 		return scriptMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFaultMediator() {
 		return faultMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_SoapVersion() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_MarkAsResponse() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultCodeSoap11() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultStringType() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultStringValue() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFaultMediator_FaultStringExpression() {
 		return (EReference)faultMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultActor() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultCodeSoap12() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultReasonType() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultReasonValue() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFaultMediator_FaultReasonExpression() {
 		return (EReference)faultMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_RoleName() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_NodeName() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultDetailType() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFaultMediator_FaultDetailValue() {
 		return (EAttribute)faultMediatorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFaultMediator_FaultDetailExpression() {
 		return (EReference)faultMediatorEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFaultMediator_InputConnector() {
 		return (EReference)faultMediatorEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFaultMediator_OutputConnector() {
 		return (EReference)faultMediatorEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFaultMediatorInputConnector() {
 		return faultMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFaultMediatorOutputConnector() {
 		return faultMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAggregateMediator() {
 		return aggregateMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAggregateMediator_AggregateID() {
 		return (EAttribute)aggregateMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_CorrelationExpression() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAggregateMediator_CompletionTimeout() {
 		return (EAttribute)aggregateMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAggregateMediator_CompletionMinMessages() {
 		return (EAttribute)aggregateMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAggregateMediator_CompletionMaxMessages() {
 		return (EAttribute)aggregateMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_InputConnector() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_OutputConnector() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_OnCompleteOutputConnector() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_MediatorFlow() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_AggregationExpression() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAggregateMediator_SequenceType() {
 		return (EAttribute)aggregateMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAggregateMediator_SequenceKey() {
 		return (EReference)aggregateMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAggregateMediatorInputConnector() {
 		return aggregateMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAggregateMediatorOutputConnector() {
 		return aggregateMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAggregateMediatorOnCompleteOutputConnector() {
 		return aggregateMediatorOnCompleteOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterMediator() {
 		return routerMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterMediator_ContinueAfterRouting() {
 		return (EAttribute)routerMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterMediator_TargetOutputConnector() {
 		return (EReference)routerMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterMediator_InputConnector() {
 		return (EReference)routerMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterMediator_OutputConnector() {
 		return (EReference)routerMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterMediator_RouterContainer() {
 		return (EReference)routerMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterRoute() {
 		return routerRouteEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterRoute_BreakAfterRoute() {
 		return (EAttribute)routerRouteEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterRoute_RouteExpression() {
 		return (EReference)routerRouteEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterRoute_RoutePattern() {
 		return (EAttribute)routerRouteEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterTarget() {
 		return routerTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterMediatorInputConnector() {
 		return routerMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterMediatorOutputConnector() {
 		return routerMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterMediatorTargetOutputConnector() {
 		return routerMediatorTargetOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterMediatorTargetOutputConnector_SoapAction() {
 		return (EAttribute)routerMediatorTargetOutputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterMediatorTargetOutputConnector_ToAddress() {
 		return (EAttribute)routerMediatorTargetOutputConnectorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterMediatorContainer() {
 		return routerMediatorContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterMediatorContainer_RouterTargetContainer() {
 		return (EReference)routerMediatorContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRouterTargetContainer() {
 		return routerTargetContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterTargetContainer_MediatorFlow() {
 		return (EReference)routerTargetContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterTargetContainer_BreakAfterRoute() {
 		return (EAttribute)routerTargetContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterTargetContainer_RouteExpression() {
 		return (EReference)routerTargetContainerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRouterTargetContainer_RoutePattern() {
 		return (EAttribute)routerTargetContainerEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRouterTargetContainer_Target() {
 		return (EReference)routerTargetContainerEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneMediator() {
 		return cloneMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneMediator_CloneID() {
 		return (EAttribute)cloneMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneMediator_SequentialMediation() {
 		return (EAttribute)cloneMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneMediator_ContinueParent() {
 		return (EAttribute)cloneMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediator_Targets() {
 		return (EReference)cloneMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediator_TargetsOutputConnector() {
 		return (EReference)cloneMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediator_InputConnector() {
 		return (EReference)cloneMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediator_OutputConnector() {
 		return (EReference)cloneMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediator_CloneContainer() {
 		return (EReference)cloneMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneTarget() {
 		return cloneTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneTarget_SoapAction() {
 		return (EAttribute)cloneTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneTarget_ToAddress() {
 		return (EAttribute)cloneTargetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneMediatorInputConnector() {
 		return cloneMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneMediatorOutputConnector() {
 		return cloneMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneMediatorTargetOutputConnector() {
 		return cloneMediatorTargetOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneMediatorTargetOutputConnector_SoapAction() {
 		return (EAttribute)cloneMediatorTargetOutputConnectorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCloneMediatorTargetOutputConnector_ToAddress() {
 		return (EAttribute)cloneMediatorTargetOutputConnectorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneMediatorContainer() {
 		return cloneMediatorContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneMediatorContainer_CloneTargetContainer() {
 		return (EReference)cloneMediatorContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCloneTargetContainer() {
 		return cloneTargetContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCloneTargetContainer_MediatorFlow() {
 		return (EReference)cloneTargetContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getIterateMediator() {
 		return iterateMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateMediator_IterateID() {
 		return (EAttribute)iterateMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateMediator_SequentialMediation() {
 		return (EAttribute)iterateMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateMediator_ContinueParent() {
 		return (EAttribute)iterateMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateMediator_PreservePayload() {
 		return (EAttribute)iterateMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_IterateExpression() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_AttachPath() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_Target() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_InputConnector() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_OutputConnector() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_TargetOutputConnector() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getIterateMediator_MediatorFlow() {
 		return (EReference)iterateMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getIterateMediatorInputConnector() {
 		return iterateMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getIterateMediatorOutputConnector() {
 		return iterateMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getIterateMediatorTargetOutputConnector() {
 		return iterateMediatorTargetOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getIterateTarget() {
 		return iterateTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateTarget_SoapAction() {
 		return (EAttribute)iterateTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getIterateTarget_ToAddress() {
 		return (EAttribute)iterateTargetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractCommonTarget() {
 		return abstractCommonTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractCommonTarget_SequenceType() {
 		return (EAttribute)abstractCommonTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractCommonTarget_Sequence() {
 		return (EReference)abstractCommonTargetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractCommonTarget_SequenceKey() {
 		return (EReference)abstractCommonTargetEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractCommonTarget_EndpointType() {
 		return (EAttribute)abstractCommonTargetEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractCommonTarget_Endpoint() {
 		return (EReference)abstractCommonTargetEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractCommonTarget_EndpointKey() {
 		return (EReference)abstractCommonTargetEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMediatorSequence() {
 		return mediatorSequenceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMediatorSequence_Anonymous() {
 		return (EAttribute)mediatorSequenceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMediatorSequence_SequenceName() {
 		return (EAttribute)mediatorSequenceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMediatorSequence_Mediators() {
 		return (EReference)mediatorSequenceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMediatorSequence_OnError() {
 		return (EReference)mediatorSequenceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMediatorSequence_Description() {
 		return (EAttribute)mediatorSequenceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCacheMediator() {
 		return cacheMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_CacheId() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_CacheScope() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_CacheAction() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_HashGenerator() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_CacheTimeout() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_MaxMessageSize() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_ImplementationType() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_MaxEntryCount() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCacheMediator_SequenceType() {
 		return (EAttribute)cacheMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCacheMediator_SequenceKey() {
 		return (EReference)cacheMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCacheMediator_InputConnector() {
 		return (EReference)cacheMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCacheMediator_OutputConnector() {
 		return (EReference)cacheMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCacheMediator_OnHitOutputConnector() {
 		return (EReference)cacheMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCacheMediator_MediatorFlow() {
 		return (EReference)cacheMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCacheMediatorInputConnector() {
 		return cacheMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCacheMediatorOutputConnector() {
 		return cacheMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCacheMediatorOnHitOutputConnector() {
 		return cacheMediatorOnHitOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCacheOnHitBranch() {
 		return cacheOnHitBranchEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXQueryMediator() {
 		return xQueryMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_Variables() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_TargetXPath() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXQueryMediator_ScriptKeyType() {
 		return (EAttribute)xQueryMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_StaticScriptKey() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_DynamicScriptKey() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_QueryKey() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_InputConnector() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryMediator_OutputConnector() {
 		return (EReference)xQueryMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXQueryMediatorInputConnector() {
 		return xQueryMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXQueryMediatorOutputConnector() {
 		return xQueryMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXQueryVariable() {
 		return xQueryVariableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXQueryVariable_VariableName() {
 		return (EAttribute)xQueryVariableEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXQueryVariable_VariableType() {
 		return (EAttribute)xQueryVariableEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXQueryVariable_ValueType() {
 		return (EAttribute)xQueryVariableEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXQueryVariable_ValueLiteral() {
 		return (EAttribute)xQueryVariableEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryVariable_ValueExpression() {
 		return (EReference)xQueryVariableEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXQueryVariable_ValueKey() {
 		return (EReference)xQueryVariableEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCalloutMediator() {
 		return calloutMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_ServiceURL() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_SoapAction() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_PathToAxis2xml() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_PathToAxis2Repository() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_PayloadType() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCalloutMediator_PayloadMessageXpath() {
 		return (EReference)calloutMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCalloutMediator_PayloadRegistryKey() {
 		return (EReference)calloutMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_ResultType() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCalloutMediator_ResultMessageXpath() {
 		return (EReference)calloutMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_ResultContextProperty() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCalloutMediator_PassHeaders() {
 		return (EAttribute)calloutMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCalloutMediator_InputConnector() {
 		return (EReference)calloutMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCalloutMediator_OutputConnector() {
 		return (EReference)calloutMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCalloutMediatorInputConnector() {
 		return calloutMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCalloutMediatorOutputConnector() {
 		return calloutMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRMSequenceMediator() {
 		return rmSequenceMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRMSequenceMediator_RmSpecVersion() {
 		return (EAttribute)rmSequenceMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRMSequenceMediator_SequenceType() {
 		return (EAttribute)rmSequenceMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRMSequenceMediator_CorrelationXpath() {
 		return (EReference)rmSequenceMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRMSequenceMediator_LastMessageXpath() {
 		return (EReference)rmSequenceMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRMSequenceMediator_InputConnector() {
 		return (EReference)rmSequenceMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRMSequenceMediator_OutputConnector() {
 		return (EReference)rmSequenceMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRMSequenceMediatorInputConnector() {
 		return rmSequenceMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRMSequenceMediatorOutputConnector() {
 		return rmSequenceMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTransactionMediator() {
 		return transactionMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTransactionMediator_Action() {
 		return (EAttribute)transactionMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTransactionMediator_InputConnector() {
 		return (EReference)transactionMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTransactionMediator_OutputConnector() {
 		return (EReference)transactionMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTransactionMediatorInputConnector() {
 		return transactionMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTransactionMediatorOutputConnector() {
 		return transactionMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOAuthMediator() {
 		return oAuthMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOAuthMediator_RemoteServiceUrl() {
 		return (EAttribute)oAuthMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getOAuthMediator_InputConnector() {
 		return (EReference)oAuthMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getOAuthMediator_OutputConnector() {
 		return (EReference)oAuthMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOAuthMediatorInputConnector() {
 		return oAuthMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOAuthMediatorOutputConnector() {
 		return oAuthMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAutoscaleInMediator() {
 		return autoscaleInMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAutoscaleOutMediator() {
 		return autoscaleOutMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getHeaderMediator() {
 		return headerMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getHeaderMediator_HeaderName() {
 		return (EReference)headerMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getHeaderMediator_HeaderAction() {
 		return (EAttribute)headerMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getHeaderMediator_ValueType() {
 		return (EAttribute)headerMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getHeaderMediator_ValueLiteral() {
 		return (EAttribute)headerMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getHeaderMediator_ValueExpression() {
 		return (EReference)headerMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getHeaderMediator_InputConnector() {
 		return (EReference)headerMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getHeaderMediator_OutputConnector() {
 		return (EReference)headerMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getHeaderMediatorInputConnector() {
 		return headerMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getHeaderMediatorOutputConnector() {
 		return headerMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottleMediator() {
 		return throttleMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleMediator_GroupId() {
 		return (EAttribute)throttleMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleMediator_PolicyType() {
 		return (EAttribute)throttleMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_PolicyKey() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleMediator_MaxConcurrentAccessCount() {
 		return (EAttribute)throttleMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_PolicyEntries() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_PolicyConfiguration() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_OnAcceptBranch() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_OnRejectBranch() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_InputConnector() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_OutputConnector() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleMediator_OnAcceptOutputConnector() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleMediator_OnRejectOutputConnector() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleMediator_ThrottleContainer() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleMediator_OnAcceptBranchsequenceType() {
 		return (EAttribute)throttleMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_OnAcceptBranchsequenceKey() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(14);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleMediator_OnRejectBranchsequenceType() {
 		return (EAttribute)throttleMediatorEClass.getEStructuralFeatures().get(15);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleMediator_OnRejectBranchsequenceKey() {
 		return (EReference)throttleMediatorEClass.getEStructuralFeatures().get(16);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottleMediatorInputConnector() {
 		return throttleMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottleMediatorOutputConnector() {
 		return throttleMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getThrottleMediatorOnAcceptOutputConnector() {
 		return throttleMediatorOnAcceptOutputConnectorEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getThrottleMediatorOnRejectOutputConnector() {
 		return throttleMediatorOnRejectOutputConnectorEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottlePolicyConfiguration() {
 		return throttlePolicyConfigurationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyConfiguration_PolicyType() {
 		return (EAttribute)throttlePolicyConfigurationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottlePolicyConfiguration_PolicyKey() {
 		return (EReference)throttlePolicyConfigurationEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyConfiguration_MaxConcurrentAccessCount() {
 		return (EAttribute)throttlePolicyConfigurationEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottlePolicyConfiguration_PolicyEntries() {
 		return (EReference)throttlePolicyConfigurationEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottlePolicyEntry() {
 		return throttlePolicyEntryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_ThrottleType() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_ThrottleRange() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_AccessType() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_MaxRequestCount() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_UnitTime() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottlePolicyEntry_ProhibitPeriod() {
 		return (EAttribute)throttlePolicyEntryEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottleOnAcceptBranch() {
 		return throttleOnAcceptBranchEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleOnAcceptBranch_SequenceType() {
 		return (EAttribute)throttleOnAcceptBranchEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleOnAcceptBranch_SequenceKey() {
 		return (EReference)throttleOnAcceptBranchEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getThrottleOnRejectBranch() {
 		return throttleOnRejectBranchEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getThrottleOnRejectBranch_SequenceType() {
 		return (EAttribute)throttleOnRejectBranchEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getThrottleOnRejectBranch_SequenceKey() {
 		return (EReference)throttleOnRejectBranchEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getThrottleContainer() {
 		return throttleContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleContainer_OnAcceptContainer() {
 		return (EReference)throttleContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleContainer_OnRejectContainer() {
 		return (EReference)throttleContainerEClass.getEStructuralFeatures().get(1);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getThrottleOnAcceptContainer() {
 		return throttleOnAcceptContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleOnAcceptContainer_MediatorFlow() {
 		return (EReference)throttleOnAcceptContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getThrottleOnRejectContainer() {
 		return throttleOnRejectContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getThrottleOnRejectContainer_MediatorFlow() {
 		return (EReference)throttleOnRejectContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCommandMediator() {
 		return commandMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandMediator_ClassName() {
 		return (EAttribute)commandMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCommandMediator_Properties() {
 		return (EReference)commandMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCommandMediator_InputConnector() {
 		return (EReference)commandMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCommandMediator_OutputConnector() {
 		return (EReference)commandMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCommandMediatorInputConnector() {
 		return commandMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCommandMediatorOutputConnector() {
 		return commandMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCommandProperty() {
 		return commandPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_PropertyName() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_ValueType() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_ValueLiteral() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_ValueContextPropertyName() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCommandProperty_ValueMessageElementXpath() {
 		return (EReference)commandPropertyEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_ContextAction() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCommandProperty_MessageAction() {
 		return (EAttribute)commandPropertyEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractSqlExecutorMediator() {
 		return abstractSqlExecutorMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionType() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionDsType() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionDbDriver() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionDsInitialContext() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionDsName() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionURL() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionUsername() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_ConnectionPassword() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyAutocommit() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyIsolation() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyMaxactive() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyMaxidle() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyMaxopenstatements() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyMaxwait() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyMinidle() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyPoolstatements() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyTestonborrow() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyTestwhileidle() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyValidationquery() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractSqlExecutorMediator_PropertyInitialsize() {
 		return (EAttribute)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractSqlExecutorMediator_SqlStatements() {
 		return (EReference)abstractSqlExecutorMediatorEClass.getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSqlStatement() {
 		return sqlStatementEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlStatement_QueryString() {
 		return (EAttribute)sqlStatementEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSqlStatement_Parameters() {
 		return (EReference)sqlStatementEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlStatement_ResultsEnabled() {
 		return (EAttribute)sqlStatementEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSqlStatement_Results() {
 		return (EReference)sqlStatementEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSqlParameterDefinition() {
 		return sqlParameterDefinitionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlParameterDefinition_DataType() {
 		return (EAttribute)sqlParameterDefinitionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlParameterDefinition_ValueType() {
 		return (EAttribute)sqlParameterDefinitionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlParameterDefinition_ValueLiteral() {
 		return (EAttribute)sqlParameterDefinitionEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSqlParameterDefinition_ValueExpression() {
 		return (EReference)sqlParameterDefinitionEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSqlResultMapping() {
 		return sqlResultMappingEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlResultMapping_PropertyName() {
 		return (EAttribute)sqlResultMappingEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSqlResultMapping_ColumnId() {
 		return (EAttribute)sqlResultMappingEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBLookupMediator() {
 		return dbLookupMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDBLookupMediator_InputConnector() {
 		return (EReference)dbLookupMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDBLookupMediator_OutputConnector() {
 		return (EReference)dbLookupMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBLookupMediatorInputConnector() {
 		return dbLookupMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBLookupMediatorOutputConnector() {
 		return dbLookupMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBReportMediator() {
 		return dbReportMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDBReportMediator_ConnectionUseTransaction() {
 		return (EAttribute)dbReportMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDBReportMediator_InputConnector() {
 		return (EReference)dbReportMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDBReportMediator_OutputConnector() {
 		return (EReference)dbReportMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBReportMediatorInputConnector() {
 		return dbReportMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDBReportMediatorOutputConnector() {
 		return dbReportMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleMediator() {
 		return ruleMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_RuleSetURI() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_RuleSetSourceType() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_RuleSetSourceCode() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_RuleSetSourceKey() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_RuleSetProperties() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_StatefulSession() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_RuleSessionProperties() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_FactsConfiguration() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_ResultsConfiguration() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_ChildMediatorsConfiguration() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_InputConnector() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_OutputConnector() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_ChildMediatorsOutputConnector() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_MediatorFlow() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_SourceValue() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_SourceXpath() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_TargetValue() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_TargetResultXpath() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleMediator_TargetXpath() {
 		return (EReference)ruleMediatorEClass.getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_TargetAction() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_InputWrapperName() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_InputNameSpace() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_OutputWrapperName() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleMediator_OutputNameSpace() {
 		return (EAttribute)ruleMediatorEClass.getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleMediatorInputConnector() {
 		return ruleMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleMediatorOutputConnector() {
 		return ruleMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleMediatorChildMediatorsOutputConnector() {
 		return ruleMediatorChildMediatorsOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleSetCreationProperty() {
 		return ruleSetCreationPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleSessionProperty() {
 		return ruleSessionPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleFactsConfiguration() {
 		return ruleFactsConfigurationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleFactsConfiguration_Facts() {
 		return (EReference)ruleFactsConfigurationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleFact() {
 		return ruleFactEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleFact_FactType() {
 		return (EAttribute)ruleFactEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleFact_FactCustomType() {
 		return (EAttribute)ruleFactEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleFact_FactName() {
 		return (EAttribute)ruleFactEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleFact_ValueType() {
 		return (EAttribute)ruleFactEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleFact_ValueLiteral() {
 		return (EAttribute)ruleFactEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleFact_ValueExpression() {
 		return (EReference)ruleFactEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleFact_ValueKey() {
 		return (EReference)ruleFactEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleResultsConfiguration() {
 		return ruleResultsConfigurationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleResultsConfiguration_Results() {
 		return (EReference)ruleResultsConfigurationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleResult() {
 		return ruleResultEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleResult_ResultType() {
 		return (EAttribute)ruleResultEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleResult_ResultCustomType() {
 		return (EAttribute)ruleResultEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleResult_ResultName() {
 		return (EAttribute)ruleResultEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleResult_ValueType() {
 		return (EAttribute)ruleResultEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRuleResult_ValueLiteral() {
 		return (EAttribute)ruleResultEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleResult_ValueExpression() {
 		return (EReference)ruleResultEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRuleResult_ValueKey() {
 		return (EReference)ruleResultEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRuleChildMediatorsConfiguration() {
 		return ruleChildMediatorsConfigurationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCallTemplateParameter() {
 		return callTemplateParameterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCallTemplateParameter_ParameterName() {
 		return (EAttribute)callTemplateParameterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCallTemplateParameter_TemplateParameterType() {
 		return (EAttribute)callTemplateParameterEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCallTemplateParameter_ParameterValue() {
 		return (EAttribute)callTemplateParameterEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCallTemplateParameter_ParameterExpression() {
 		return (EReference)callTemplateParameterEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCallTemplateMediator() {
 		return callTemplateMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCallTemplateMediator_TargetTemplate() {
 		return (EAttribute)callTemplateMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCallTemplateMediator_TemplateParameters() {
 		return (EReference)callTemplateMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCallTemplateMediator_InputConnector() {
 		return (EReference)callTemplateMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getCallTemplateMediator_OutputConnector() {
 		return (EReference)callTemplateMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCallTemplateMediatorInputConnector() {
 		return callTemplateMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCallTemplateMediatorOutputConnector() {
 		return callTemplateMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSmooksMediator() {
 		return smooksMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSmooksMediator_ConfigurationKey() {
 		return (EReference)smooksMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSmooksMediator_InputType() {
 		return (EAttribute)smooksMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSmooksMediator_InputExpression() {
 		return (EReference)smooksMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSmooksMediator_OutputType() {
 		return (EAttribute)smooksMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSmooksMediator_OutputExpression() {
 		return (EReference)smooksMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSmooksMediator_OutputProperty() {
 		return (EAttribute)smooksMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSmooksMediator_OutputAction() {
 		return (EAttribute)smooksMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSmooksMediator_OutputMethod() {
 		return (EAttribute)smooksMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSmooksMediator_InputConnector() {
 		return (EReference)smooksMediatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSmooksMediator_OutputConnector() {
 		return (EReference)smooksMediatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSmooksMediatorInputConnector() {
 		return smooksMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSmooksMediatorOutputConnector() {
 		return smooksMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getStoreMediator() {
 		return storeMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStoreMediator_MessageStore() {
 		return (EAttribute)storeMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getStoreMediator_OnStoreSequence() {
 		return (EReference)storeMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getStoreMediator_InputConnector() {
 		return (EReference)storeMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getStoreMediator_OutputConnector() {
 		return (EReference)storeMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getStoreMediatorInputConnector() {
 		return storeMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getStoreMediatorOutputConnector() {
 		return storeMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBuilderMediator() {
 		return builderMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBuilderMediator_MessageBuilders() {
 		return (EReference)builderMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBuilderMediator_InputConnector() {
 		return (EReference)builderMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBuilderMediator_OutputConnector() {
 		return (EReference)builderMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBuilderMediatorInputConnector() {
 		return builderMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBuilderMediatorOutputConector() {
 		return builderMediatorOutputConectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMessageBuilder() {
 		return messageBuilderEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMessageBuilder_ContentType() {
 		return (EAttribute)messageBuilderEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMessageBuilder_BuilderClass() {
 		return (EAttribute)messageBuilderEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMessageBuilder_FormatterClass() {
 		return (EAttribute)messageBuilderEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPayloadFactoryMediator() {
 		return payloadFactoryMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPayloadFactoryMediator_Format() {
 		return (EAttribute)payloadFactoryMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPayloadFactoryMediator_Args() {
 		return (EReference)payloadFactoryMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPayloadFactoryMediator_InputConnector() {
 		return (EReference)payloadFactoryMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPayloadFactoryMediator_OutputConnector() {
 		return (EReference)payloadFactoryMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPayloadFactoryMediatorInputConnector() {
 		return payloadFactoryMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPayloadFactoryMediatorOutputConnector() {
 		return payloadFactoryMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPayloadFactoryArgument() {
 		return payloadFactoryArgumentEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPayloadFactoryArgument_ArgumentType() {
 		return (EAttribute)payloadFactoryArgumentEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPayloadFactoryArgument_ArgumentValue() {
 		return (EAttribute)payloadFactoryArgumentEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPayloadFactoryArgument_ArgumentExpression() {
 		return (EReference)payloadFactoryArgumentEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConditionalRouteBranch() {
 		return conditionalRouteBranchEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getConditionalRouteBranch_BreakAfterRoute() {
 		return (EAttribute)conditionalRouteBranchEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouteBranch_EvaluatorExpression() {
 		return (EReference)conditionalRouteBranchEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouteBranch_TargetSequence() {
 		return (EReference)conditionalRouteBranchEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConditionalRouterMediator() {
 		return conditionalRouterMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getConditionalRouterMediator_ContinueRoute() {
 		return (EAttribute)conditionalRouterMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouterMediator_ConditionalRouteBranches() {
 		return (EReference)conditionalRouterMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouterMediator_InputConnector() {
 		return (EReference)conditionalRouterMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouterMediator_OutputConnector() {
 		return (EReference)conditionalRouterMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouterMediator_AdditionalOutputConnector() {
 		return (EReference)conditionalRouterMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConditionalRouterMediator_MediatorFlow() {
 		return (EReference)conditionalRouterMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConditionalRouterMediatorInputConnector() {
 		return conditionalRouterMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConditionalRouterMediatorOutputConnector() {
 		return conditionalRouterMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConditionalRouterMediatorAdditionalOutputConnector() {
 		return conditionalRouterMediatorAdditionalOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSendMediator() {
 		return sendMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSendMediator_EndPoint() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSendMediator_InputConnector() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSendMediator_OutputConnector() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSendMediator_ReceivingSequenceType() {
 		return (EAttribute)sendMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSendMediator_StaticReceivingSequence() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSendMediator_DynamicReceivingSequence() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getSendMediator_EndpointOutputConnector() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(6);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getSendMediator_EndpointFlow() {
 		return (EReference)sendMediatorEClass.getEStructuralFeatures().get(7);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getSendContainer() {
 		return sendContainerEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EReference getSendContainer_EndpointFlow() {
 		return (EReference)sendContainerEClass.getEStructuralFeatures().get(0);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSendMediatorInputConnector() {
 		return sendMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSendMediatorOutputConnector() {
 		return sendMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public EClass getSendMediatorEndpointOutputConnector() {
 		return sendMediatorEndpointOutputConnectorEClass;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFailoverEndPoint() {
 		return failoverEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFailoverEndPoint_InputConnector() {
 		return (EReference)failoverEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFailoverEndPoint_OutputConnector() {
 		return (EReference)failoverEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFailoverEndPoint_WestOutputConnector() {
 		return (EReference)failoverEndPointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFailoverEndPointInputConnector() {
 		return failoverEndPointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFailoverEndPointOutputConnector() {
 		return failoverEndPointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFailoverEndPointWestOutputConnector() {
 		return failoverEndPointWestOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getParentEndPoint() {
 		return parentEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getParentEndPoint_Children() {
 		return (EReference)parentEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getWSDLEndPoint() {
 		return wsdlEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getWSDLEndPoint_WSDLDefinition() {
 		return (EReference)wsdlEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getWSDLEndPoint_WSDLDescription() {
 		return (EReference)wsdlEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getWSDLEndPoint_InputConnector() {
 		return (EReference)wsdlEndPointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getWSDLEndPoint_OutputConnector() {
 		return (EReference)wsdlEndPointEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getWSDLEndPoint_WsdlUri() {
 		return (EAttribute)wsdlEndPointEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getWSDLEndPoint_Service() {
 		return (EAttribute)wsdlEndPointEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getWSDLEndPoint_Port() {
 		return (EAttribute)wsdlEndPointEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getWSDLDefinition() {
 		return wsdlDefinitionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getWSDLDescription() {
 		return wsdlDescriptionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getWSDLEndPointInputConnector() {
 		return wsdlEndPointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getWSDLEndPointOutputConnector() {
 		return wsdlEndPointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLoadBalanceEndPoint() {
 		return loadBalanceEndPointEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLoadBalanceEndPoint_Session() {
 		return (EReference)loadBalanceEndPointEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLoadBalanceEndPoint_Failover() {
 		return (EAttribute)loadBalanceEndPointEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLoadBalanceEndPoint_Policy() {
 		return (EAttribute)loadBalanceEndPointEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLoadBalanceEndPoint_InputConnector() {
 		return (EReference)loadBalanceEndPointEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLoadBalanceEndPoint_OutputConnector() {
 		return (EReference)loadBalanceEndPointEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getLoadBalanceEndPoint_WestOutputConnector() {
 		return (EReference)loadBalanceEndPointEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLoadBalanceEndPointInputConnector() {
 		return loadBalanceEndPointInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLoadBalanceEndPointOutputConnector() {
 		return loadBalanceEndPointOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLoadBalanceEndPointWestOutputConnector() {
 		return loadBalanceEndPointWestOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLocalEntry() {
 		return localEntryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalEntry_EntryName() {
 		return (EAttribute)localEntryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalEntry_ValueType() {
 		return (EAttribute)localEntryEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalEntry_ValueLiteral() {
 		return (EAttribute)localEntryEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalEntry_ValueXML() {
 		return (EAttribute)localEntryEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalEntry_ValueURL() {
 		return (EAttribute)localEntryEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSession() {
 		return sessionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSession_Type() {
 		return (EAttribute)sessionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequences() {
 		return sequencesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequences_MediatorFlow() {
 		return (EReference)sequencesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSequences_Name() {
 		return (EAttribute)sequencesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequences_OutputConnector() {
 		return (EReference)sequencesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSequences_InputConnector() {
 		return (EReference)sequencesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequencesOutputConnector() {
 		return sequencesOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSequencesInputConnector() {
 		return sequencesInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getURLRewriteRuleAction() {
 		return urlRewriteRuleActionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteRuleAction_RuleAction() {
 		return (EAttribute)urlRewriteRuleActionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteRuleAction_RuleFragment() {
 		return (EAttribute)urlRewriteRuleActionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteRuleAction_RuleOption() {
 		return (EAttribute)urlRewriteRuleActionEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteRuleAction_ActionExpression() {
 		return (EReference)urlRewriteRuleActionEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteRuleAction_ActionValue() {
 		return (EAttribute)urlRewriteRuleActionEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteRuleAction_ActionRegex() {
 		return (EAttribute)urlRewriteRuleActionEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getURLRewriteRule() {
 		return urlRewriteRuleEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteRule_UrlRewriteRuleCondition() {
 		return (EReference)urlRewriteRuleEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteRule_RewriteRuleAction() {
 		return (EReference)urlRewriteRuleEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getURLRewriteMediator() {
 		return urlRewriteMediatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteMediator_UrlRewriteRules() {
 		return (EReference)urlRewriteMediatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteMediator_InProperty() {
 		return (EAttribute)urlRewriteMediatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getURLRewriteMediator_OutProperty() {
 		return (EAttribute)urlRewriteMediatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteMediator_InputConnector() {
 		return (EReference)urlRewriteMediatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getURLRewriteMediator_OutputConnector() {
 		return (EReference)urlRewriteMediatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getURLRewriteMediatorInputConnector() {
 		return urlRewriteMediatorInputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getURLRewriteMediatorOutputConnector() {
 		return urlRewriteMediatorOutputConnectorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEvaluatorExpressionProperty() {
 		return evaluatorExpressionPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEvaluatorExpressionProperty_PrettyName() {
 		return (EAttribute)evaluatorExpressionPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEvaluatorExpressionProperty_EvaluatorName() {
 		return (EAttribute)evaluatorExpressionPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEvaluatorExpressionProperty_EvaluatorValue() {
 		return (EAttribute)evaluatorExpressionPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getProxyWsdlType() {
 		return proxyWsdlTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFilterConditionType() {
 		return filterConditionTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getLogCategory() {
 		return logCategoryEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getLogLevel() {
 		return logLevelEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEndPointAddressingVersion() {
 		return endPointAddressingVersionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEndPointTimeOutAction() {
 		return endPointTimeOutActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEndPointMessageFormat() {
 		return endPointMessageFormatEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEndPointAttachmentOptimization() {
 		return endPointAttachmentOptimizationEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getPropertyDataType() {
 		return propertyDataTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getPropertyAction() {
 		return propertyActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getPropertyScope() {
 		return propertyScopeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getPropertyValueType() {
 		return propertyValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEnrichSourceType() {
 		return enrichSourceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEnrichTargetAction() {
 		return enrichTargetActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEnrichTargetType() {
 		return enrichTargetTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getEventTopicType() {
 		return eventTopicTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getScriptType() {
 		return scriptTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getScriptLanguage() {
 		return scriptLanguageEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultSoapVersion() {
 		return faultSoapVersionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultCodeSoap11() {
 		return faultCodeSoap11EEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultCodeSoap12() {
 		return faultCodeSoap12EEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultStringType() {
 		return faultStringTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultReasonType() {
 		return faultReasonTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFaultDetailType() {
 		return faultDetailTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getAggregateSequenceType() {
 		return aggregateSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTargetSequenceType() {
 		return targetSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTargetEndpointType() {
 		return targetEndpointTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCacheSequenceType() {
 		return cacheSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCacheImplementationType() {
 		return cacheImplementationTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCacheAction() {
 		return cacheActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCacheScope() {
 		return cacheScopeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getXQueryVariableType() {
 		return xQueryVariableTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getXQueryVariableValueType() {
 		return xQueryVariableValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCalloutPayloadType() {
 		return calloutPayloadTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCalloutResultType() {
 		return calloutResultTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRMSpecVersion() {
 		return rmSpecVersionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRMSequenceType() {
 		return rmSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTransactionAction() {
 		return transactionActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getHeaderAction() {
 		return headerActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getHeaderValueType() {
 		return headerValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getThrottlePolicyType() {
 		return throttlePolicyTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getThrottleConditionType() {
 		return throttleConditionTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getThrottleAccessType() {
 		return throttleAccessTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getThrottleSequenceType() {
 		return throttleSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCommandPropertyValueType() {
 		return commandPropertyValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCommandPropertyMessageAction() {
 		return commandPropertyMessageActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getCommandPropertyContextAction() {
 		return commandPropertyContextActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlExecutorConnectionType() {
 		return sqlExecutorConnectionTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlExecutorDatasourceType() {
 		return sqlExecutorDatasourceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlExecutorBooleanValue() {
 		return sqlExecutorBooleanValueEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlExecutorIsolationLevel() {
 		return sqlExecutorIsolationLevelEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlParameterValueType() {
 		return sqlParameterValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSqlParameterDataType() {
 		return sqlParameterDataTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleActions() {
 		return ruleActionsEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleSourceType() {
 		return ruleSourceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleFactType() {
 		return ruleFactTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleFactValueType() {
 		return ruleFactValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleResultType() {
 		return ruleResultTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleResultValueType() {
 		return ruleResultValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleOptionType() {
 		return ruleOptionTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getSmooksIODataType() {
 		return smooksIODataTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getExpressionAction() {
 		return expressionActionEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getOutputMethod() {
 		return outputMethodEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getReceivingSequenceType() {
 		return receivingSequenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getKeyType() {
 		return keyTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getPayloadFactoryArgumentType() {
 		return payloadFactoryArgumentTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getType() {
 		return typeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getLocalEntryValueType() {
 		return localEntryValueTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleActionType() {
 		return ruleActionTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRuleFragmentType() {
 		return ruleFragmentTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTemplateType() {
 		return templateTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTaskPropertyType() {
 		return taskPropertyTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getTaskTriggerType() {
 		return taskTriggerTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getApiResourceUrlStyle() {
 		return apiResourceUrlStyleEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getMap() {
 		return mapEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EsbFactory getEsbFactory() {
 		return (EsbFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isCreated = false;
 
 	/**
 	 * Creates the meta-model objects for the package.  This method is
 	 * guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void createPackageContents() {
 		if (isCreated) return;
 		isCreated = true;
 
 		// Create classes and their features
 		esbDiagramEClass = createEClass(ESB_DIAGRAM);
 		createEReference(esbDiagramEClass, ESB_DIAGRAM__SERVER);
 		createEAttribute(esbDiagramEClass, ESB_DIAGRAM__TEST);
 
 		esbNodeEClass = createEClass(ESB_NODE);
 
 		esbElementEClass = createEClass(ESB_ELEMENT);
 
 		esbServerEClass = createEClass(ESB_SERVER);
 		createEReference(esbServerEClass, ESB_SERVER__CHILDREN);
 		createEReference(esbServerEClass, ESB_SERVER__MESSAGE_MEDIATOR);
 		createEAttribute(esbServerEClass, ESB_SERVER__TYPE);
 
 		mediatorEClass = createEClass(MEDIATOR);
 		createEAttribute(mediatorEClass, MEDIATOR__REVERSE);
 
 		esbConnectorEClass = createEClass(ESB_CONNECTOR);
 
 		inputConnectorEClass = createEClass(INPUT_CONNECTOR);
 		createEReference(inputConnectorEClass, INPUT_CONNECTOR__INCOMING_LINKS);
 
 		outputConnectorEClass = createEClass(OUTPUT_CONNECTOR);
 		createEReference(outputConnectorEClass, OUTPUT_CONNECTOR__OUTGOING_LINK);
 
 		additionalOutputConnectorEClass = createEClass(ADDITIONAL_OUTPUT_CONNECTOR);
 		createEReference(additionalOutputConnectorEClass, ADDITIONAL_OUTPUT_CONNECTOR__ADDITIONAL_OUTGOING_LINK);
 
 		esbLinkEClass = createEClass(ESB_LINK);
 		createEReference(esbLinkEClass, ESB_LINK__SOURCE);
 		createEReference(esbLinkEClass, ESB_LINK__TARGET);
 
 		endPointEClass = createEClass(END_POINT);
 		createEAttribute(endPointEClass, END_POINT__END_POINT_NAME);
 		createEAttribute(endPointEClass, END_POINT__ANONYMOUS);
 
 		proxyServiceEClass = createEClass(PROXY_SERVICE);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__OUTPUT_CONNECTOR);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__INPUT_CONNECTOR);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__FAULT_INPUT_CONNECTOR);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__NAME);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__PINNED_SERVERS);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__SERVICE_GROUP);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__TRACE_ENABLED);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__STATISTICS_ENABLED);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__TRANSPORTS);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__RELIABLE_MESSAGING_ENABLED);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__SECURITY_ENABLED);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__WSDL_TYPE);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__WSDL_XML);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__WSDL_URL);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__WSDL_KEY);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__SERVICE_PARAMETERS);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__SERVICE_POLICIES);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__CONTAINER);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__IN_SEQUENCE_TYPE);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__IN_SEQUENCE_KEY);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__IN_SEQUENCE_NAME);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__OUT_SEQUENCE_TYPE);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__OUT_SEQUENCE_KEY);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__OUT_SEQUENCE_NAME);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__FAULT_SEQUENCE_TYPE);
 		createEReference(proxyServiceEClass, PROXY_SERVICE__FAULT_SEQUENCE_KEY);
 		createEAttribute(proxyServiceEClass, PROXY_SERVICE__FAULT_SEQUENCE_NAME);
 
 		proxyOutputConnectorEClass = createEClass(PROXY_OUTPUT_CONNECTOR);
 
 		proxyInputConnectorEClass = createEClass(PROXY_INPUT_CONNECTOR);
 
 		proxyFaultInputConnectorEClass = createEClass(PROXY_FAULT_INPUT_CONNECTOR);
 
 		proxyServiceParameterEClass = createEClass(PROXY_SERVICE_PARAMETER);
 		createEAttribute(proxyServiceParameterEClass, PROXY_SERVICE_PARAMETER__NAME);
 		createEAttribute(proxyServiceParameterEClass, PROXY_SERVICE_PARAMETER__VALUE);
 
 		proxyServicePolicyEClass = createEClass(PROXY_SERVICE_POLICY);
 		createEReference(proxyServicePolicyEClass, PROXY_SERVICE_POLICY__POLICY_KEY);
 
 		proxyServiceSequenceAndEndpointContainerEClass = createEClass(PROXY_SERVICE_SEQUENCE_AND_ENDPOINT_CONTAINER);
 		createEReference(proxyServiceSequenceAndEndpointContainerEClass, PROXY_SERVICE_SEQUENCE_AND_ENDPOINT_CONTAINER__MEDIATOR_FLOW);
 
 		proxyServiceFaultContainerEClass = createEClass(PROXY_SERVICE_FAULT_CONTAINER);
 		createEReference(proxyServiceFaultContainerEClass, PROXY_SERVICE_FAULT_CONTAINER__MEDIATOR_FLOW);
 
 		proxyServiceContainerEClass = createEClass(PROXY_SERVICE_CONTAINER);
 		createEReference(proxyServiceContainerEClass, PROXY_SERVICE_CONTAINER__SEQUENCE_AND_ENDPOINT_CONTAINER);
 		createEReference(proxyServiceContainerEClass, PROXY_SERVICE_CONTAINER__FAULT_CONTAINER);
 
 		mediatorFlowEClass = createEClass(MEDIATOR_FLOW);
 		createEReference(mediatorFlowEClass, MEDIATOR_FLOW__CHILDREN);
 
 		endpointFlowEClass = createEClass(ENDPOINT_FLOW);
 		createEReference(endpointFlowEClass, ENDPOINT_FLOW__CHILDREN);
 
 		abstractEndPointEClass = createEClass(ABSTRACT_END_POINT);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__RELIABLE_MESSAGING_ENABLED);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__SECURITY_ENABLED);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__ADDRESSING_ENABLED);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__ADDRESSING_VERSION);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__ADDRESSING_SEPARATE_LISTENER);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__TIME_OUT_DURATION);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__TIME_OUT_ACTION);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__RETRY_ERROR_CODES);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__RETRY_COUNT);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__RETRY_DELAY);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__SUSPEND_ERROR_CODES);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__SUSPEND_INITIAL_DURATION);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__SUSPEND_MAXIMUM_DURATION);
 		createEAttribute(abstractEndPointEClass, ABSTRACT_END_POINT__SUSPEND_PROGRESSION_FACTOR);
 		createEReference(abstractEndPointEClass, ABSTRACT_END_POINT__RELIABLE_MESSAGING_POLICY);
 		createEReference(abstractEndPointEClass, ABSTRACT_END_POINT__SECURITY_POLICY);
 
 		messageMediatorEClass = createEClass(MESSAGE_MEDIATOR);
 		createEReference(messageMediatorEClass, MESSAGE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(messageMediatorEClass, MESSAGE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		messageInputConnectorEClass = createEClass(MESSAGE_INPUT_CONNECTOR);
 
 		messageOutputConnectorEClass = createEClass(MESSAGE_OUTPUT_CONNECTOR);
 
 		defaultEndPointEClass = createEClass(DEFAULT_END_POINT);
 		createEReference(defaultEndPointEClass, DEFAULT_END_POINT__INPUT_CONNECTOR);
 		createEReference(defaultEndPointEClass, DEFAULT_END_POINT__OUTPUT_CONNECTOR);
 
 		defaultEndPointInputConnectorEClass = createEClass(DEFAULT_END_POINT_INPUT_CONNECTOR);
 
 		defaultEndPointOutputConnectorEClass = createEClass(DEFAULT_END_POINT_OUTPUT_CONNECTOR);
 
 		addressEndPointEClass = createEClass(ADDRESS_END_POINT);
 		createEReference(addressEndPointEClass, ADDRESS_END_POINT__INPUT_CONNECTOR);
 		createEReference(addressEndPointEClass, ADDRESS_END_POINT__OUTPUT_CONNECTOR);
 		createEAttribute(addressEndPointEClass, ADDRESS_END_POINT__URI);
 
 		addressEndPointInputConnectorEClass = createEClass(ADDRESS_END_POINT_INPUT_CONNECTOR);
 
 		addressEndPointOutputConnectorEClass = createEClass(ADDRESS_END_POINT_OUTPUT_CONNECTOR);
 
 		dropMediatorEClass = createEClass(DROP_MEDIATOR);
 		createEReference(dropMediatorEClass, DROP_MEDIATOR__INPUT_CONNECTOR);
 
 		dropMediatorInputConnectorEClass = createEClass(DROP_MEDIATOR_INPUT_CONNECTOR);
 
 		filterMediatorEClass = createEClass(FILTER_MEDIATOR);
 		createEAttribute(filterMediatorEClass, FILTER_MEDIATOR__CONDITION_TYPE);
 		createEAttribute(filterMediatorEClass, FILTER_MEDIATOR__REGEX);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__PASS_OUTPUT_CONNECTOR);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__FAIL_OUTPUT_CONNECTOR);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__XPATH);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__SOURCE);
 		createEReference(filterMediatorEClass, FILTER_MEDIATOR__FILTER_CONTAINER);
 
 		filterContainerEClass = createEClass(FILTER_CONTAINER);
 		createEReference(filterContainerEClass, FILTER_CONTAINER__PASS_CONTAINER);
 		createEReference(filterContainerEClass, FILTER_CONTAINER__FAIL_CONTAINER);
 
 		filterPassContainerEClass = createEClass(FILTER_PASS_CONTAINER);
 		createEReference(filterPassContainerEClass, FILTER_PASS_CONTAINER__MEDIATOR_FLOW);
 
 		filterFailContainerEClass = createEClass(FILTER_FAIL_CONTAINER);
 		createEReference(filterFailContainerEClass, FILTER_FAIL_CONTAINER__MEDIATOR_FLOW);
 
 		filterMediatorInputConnectorEClass = createEClass(FILTER_MEDIATOR_INPUT_CONNECTOR);
 
 		filterMediatorOutputConnectorEClass = createEClass(FILTER_MEDIATOR_OUTPUT_CONNECTOR);
 
 		filterMediatorPassOutputConnectorEClass = createEClass(FILTER_MEDIATOR_PASS_OUTPUT_CONNECTOR);
 
 		filterMediatorFailOutputConnectorEClass = createEClass(FILTER_MEDIATOR_FAIL_OUTPUT_CONNECTOR);
 
 		mergeNodeEClass = createEClass(MERGE_NODE);
 		createEReference(mergeNodeEClass, MERGE_NODE__FIRST_INPUT_CONNECTOR);
 		createEReference(mergeNodeEClass, MERGE_NODE__SECOND_INPUT_CONNECTOR);
 		createEReference(mergeNodeEClass, MERGE_NODE__OUTPUT_CONNECTOR);
 
 		mergeNodeFirstInputConnectorEClass = createEClass(MERGE_NODE_FIRST_INPUT_CONNECTOR);
 
 		mergeNodeSecondInputConnectorEClass = createEClass(MERGE_NODE_SECOND_INPUT_CONNECTOR);
 
 		mergeNodeOutputConnectorEClass = createEClass(MERGE_NODE_OUTPUT_CONNECTOR);
 
 		logMediatorEClass = createEClass(LOG_MEDIATOR);
 		createEAttribute(logMediatorEClass, LOG_MEDIATOR__LOG_CATEGORY);
 		createEAttribute(logMediatorEClass, LOG_MEDIATOR__LOG_LEVEL);
 		createEAttribute(logMediatorEClass, LOG_MEDIATOR__LOG_SEPARATOR);
 		createEReference(logMediatorEClass, LOG_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(logMediatorEClass, LOG_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(logMediatorEClass, LOG_MEDIATOR__PROPERTIES);
 
 		logMediatorInputConnectorEClass = createEClass(LOG_MEDIATOR_INPUT_CONNECTOR);
 
 		logMediatorOutputConnectorEClass = createEClass(LOG_MEDIATOR_OUTPUT_CONNECTOR);
 
 		logPropertyEClass = createEClass(LOG_PROPERTY);
 
 		registryKeyPropertyEClass = createEClass(REGISTRY_KEY_PROPERTY);
 		createEAttribute(registryKeyPropertyEClass, REGISTRY_KEY_PROPERTY__PRETTY_NAME);
 		createEAttribute(registryKeyPropertyEClass, REGISTRY_KEY_PROPERTY__KEY_NAME);
 		createEAttribute(registryKeyPropertyEClass, REGISTRY_KEY_PROPERTY__KEY_VALUE);
 		createEAttribute(registryKeyPropertyEClass, REGISTRY_KEY_PROPERTY__FILTERS);
 
 		propertyMediatorEClass = createEClass(PROPERTY_MEDIATOR);
 		createEReference(propertyMediatorEClass, PROPERTY_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(propertyMediatorEClass, PROPERTY_MEDIATOR__OUTPUT_CONNECTOR);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__PROPERTY_NAME);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__PROPERTY_DATA_TYPE);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__PROPERTY_ACTION);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__PROPERTY_SCOPE);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_TYPE);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_LITERAL);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__EXPRESSION);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__NAMESPACE_PREFIX);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__NAMESPACE);
 		createEReference(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_EXPRESSION);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_OM);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_STRING_PATTERN);
 		createEAttribute(propertyMediatorEClass, PROPERTY_MEDIATOR__VALUE_STRING_CAPTURING_GROUP);
 
 		propertyMediatorInputConnectorEClass = createEClass(PROPERTY_MEDIATOR_INPUT_CONNECTOR);
 
 		propertyMediatorOutputConnectorEClass = createEClass(PROPERTY_MEDIATOR_OUTPUT_CONNECTOR);
 
 		namespacedPropertyEClass = createEClass(NAMESPACED_PROPERTY);
 		createEAttribute(namespacedPropertyEClass, NAMESPACED_PROPERTY__PRETTY_NAME);
 		createEAttribute(namespacedPropertyEClass, NAMESPACED_PROPERTY__PROPERTY_NAME);
 		createEAttribute(namespacedPropertyEClass, NAMESPACED_PROPERTY__PROPERTY_VALUE);
 		createEAttribute(namespacedPropertyEClass, NAMESPACED_PROPERTY__NAMESPACES);
 
 		enrichMediatorEClass = createEClass(ENRICH_MEDIATOR);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__CLONE_SOURCE);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__SOURCE_TYPE);
 		createEReference(enrichMediatorEClass, ENRICH_MEDIATOR__SOURCE_XPATH);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__SOURCE_PROPERTY);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__SOURCE_XML);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__TARGET_ACTION);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__TARGET_TYPE);
 		createEReference(enrichMediatorEClass, ENRICH_MEDIATOR__TARGET_XPATH);
 		createEAttribute(enrichMediatorEClass, ENRICH_MEDIATOR__TARGET_PROPERTY);
 		createEReference(enrichMediatorEClass, ENRICH_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(enrichMediatorEClass, ENRICH_MEDIATOR__OUTPUT_CONNECTOR);
 
 		enrichMediatorInputConnectorEClass = createEClass(ENRICH_MEDIATOR_INPUT_CONNECTOR);
 
 		enrichMediatorOutputConnectorEClass = createEClass(ENRICH_MEDIATOR_OUTPUT_CONNECTOR);
 
 		abstractNameValueExpressionPropertyEClass = createEClass(ABSTRACT_NAME_VALUE_EXPRESSION_PROPERTY);
 		createEAttribute(abstractNameValueExpressionPropertyEClass, ABSTRACT_NAME_VALUE_EXPRESSION_PROPERTY__PROPERTY_NAME);
 		createEAttribute(abstractNameValueExpressionPropertyEClass, ABSTRACT_NAME_VALUE_EXPRESSION_PROPERTY__PROPERTY_VALUE_TYPE);
 		createEAttribute(abstractNameValueExpressionPropertyEClass, ABSTRACT_NAME_VALUE_EXPRESSION_PROPERTY__PROPERTY_VALUE);
 		createEReference(abstractNameValueExpressionPropertyEClass, ABSTRACT_NAME_VALUE_EXPRESSION_PROPERTY__PROPERTY_EXPRESSION);
 
 		abstractBooleanFeatureEClass = createEClass(ABSTRACT_BOOLEAN_FEATURE);
 		createEAttribute(abstractBooleanFeatureEClass, ABSTRACT_BOOLEAN_FEATURE__FEATURE_NAME);
 		createEAttribute(abstractBooleanFeatureEClass, ABSTRACT_BOOLEAN_FEATURE__FEATURE_ENABLED);
 
 		abstractLocationKeyResourceEClass = createEClass(ABSTRACT_LOCATION_KEY_RESOURCE);
 		createEAttribute(abstractLocationKeyResourceEClass, ABSTRACT_LOCATION_KEY_RESOURCE__LOCATION);
 		createEReference(abstractLocationKeyResourceEClass, ABSTRACT_LOCATION_KEY_RESOURCE__KEY);
 
 		xsltMediatorEClass = createEClass(XSLT_MEDIATOR);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__OUTPUT_CONNECTOR);
 		createEAttribute(xsltMediatorEClass, XSLT_MEDIATOR__XSLT_SCHEMA_KEY_TYPE);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__XSLT_STATIC_SCHEMA_KEY);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__XSLT_DYNAMIC_SCHEMA_KEY);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__XSLT_KEY);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__SOURCE_XPATH);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__PROPERTIES);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__FEATURES);
 		createEReference(xsltMediatorEClass, XSLT_MEDIATOR__RESOURCES);
 
 		xsltPropertyEClass = createEClass(XSLT_PROPERTY);
 
 		xsltFeatureEClass = createEClass(XSLT_FEATURE);
 
 		xsltResourceEClass = createEClass(XSLT_RESOURCE);
 
 		xsltMediatorInputConnectorEClass = createEClass(XSLT_MEDIATOR_INPUT_CONNECTOR);
 
 		xsltMediatorOutputConnectorEClass = createEClass(XSLT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		switchMediatorEClass = createEClass(SWITCH_MEDIATOR);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__SOURCE_XPATH);
 		createEAttribute(switchMediatorEClass, SWITCH_MEDIATOR__SOURCE);
 		createEAttribute(switchMediatorEClass, SWITCH_MEDIATOR__NAMESPACE);
 		createEAttribute(switchMediatorEClass, SWITCH_MEDIATOR__NAMESPACE_PREFIX);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__CASE_BRANCHES);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__DEFAULT_BRANCH);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(switchMediatorEClass, SWITCH_MEDIATOR__SWITCH_CONTAINER);
 
 		switchCaseBranchOutputConnectorEClass = createEClass(SWITCH_CASE_BRANCH_OUTPUT_CONNECTOR);
 		createEAttribute(switchCaseBranchOutputConnectorEClass, SWITCH_CASE_BRANCH_OUTPUT_CONNECTOR__CASE_REGEX);
 
 		switchDefaultBranchOutputConnectorEClass = createEClass(SWITCH_DEFAULT_BRANCH_OUTPUT_CONNECTOR);
 
 		switchMediatorInputConnectorEClass = createEClass(SWITCH_MEDIATOR_INPUT_CONNECTOR);
 
 		switchMediatorOutputConnectorEClass = createEClass(SWITCH_MEDIATOR_OUTPUT_CONNECTOR);
 
 		switchMediatorContainerEClass = createEClass(SWITCH_MEDIATOR_CONTAINER);
 		createEReference(switchMediatorContainerEClass, SWITCH_MEDIATOR_CONTAINER__SWITCH_CASE_CONTAINER);
 		createEReference(switchMediatorContainerEClass, SWITCH_MEDIATOR_CONTAINER__SWITCH_DEFAULT_CONTAINER);
 
 		switchCaseContainerEClass = createEClass(SWITCH_CASE_CONTAINER);
 		createEReference(switchCaseContainerEClass, SWITCH_CASE_CONTAINER__MEDIATOR_FLOW);
 
 		switchDefaultContainerEClass = createEClass(SWITCH_DEFAULT_CONTAINER);
 		createEReference(switchDefaultContainerEClass, SWITCH_DEFAULT_CONTAINER__MEDIATOR_FLOW);
 
 		sequenceDiagramEClass = createEClass(SEQUENCE_DIAGRAM);
 		createEReference(sequenceDiagramEClass, SEQUENCE_DIAGRAM__SEQUENCE);
 
 		esbSequenceEClass = createEClass(ESB_SEQUENCE);
 		createEAttribute(esbSequenceEClass, ESB_SEQUENCE__NAME);
 		createEReference(esbSequenceEClass, ESB_SEQUENCE__INPUT);
 		createEReference(esbSequenceEClass, ESB_SEQUENCE__OUTPUT);
 		createEReference(esbSequenceEClass, ESB_SEQUENCE__CHILD_MEDIATORS);
 
 		esbSequenceInputEClass = createEClass(ESB_SEQUENCE_INPUT);
 		createEReference(esbSequenceInputEClass, ESB_SEQUENCE_INPUT__CONNECTOR);
 
 		esbSequenceOutputEClass = createEClass(ESB_SEQUENCE_OUTPUT);
 		createEReference(esbSequenceOutputEClass, ESB_SEQUENCE_OUTPUT__CONNECTOR);
 
 		esbSequenceInputConnectorEClass = createEClass(ESB_SEQUENCE_INPUT_CONNECTOR);
 
 		esbSequenceOutputConnectorEClass = createEClass(ESB_SEQUENCE_OUTPUT_CONNECTOR);
 
 		sequenceEClass = createEClass(SEQUENCE);
 		createEAttribute(sequenceEClass, SEQUENCE__NAME);
 		createEAttribute(sequenceEClass, SEQUENCE__KEY);
 		createEReference(sequenceEClass, SEQUENCE__INPUT_CONNECTOR);
 		createEReference(sequenceEClass, SEQUENCE__OUTPUT_CONNECTOR);
 		createEReference(sequenceEClass, SEQUENCE__INCLUDED_MEDIATORS);
 
 		sequenceInputConnectorEClass = createEClass(SEQUENCE_INPUT_CONNECTOR);
 
 		sequenceOutputConnectorEClass = createEClass(SEQUENCE_OUTPUT_CONNECTOR);
 
 		eventMediatorEClass = createEClass(EVENT_MEDIATOR);
 		createEAttribute(eventMediatorEClass, EVENT_MEDIATOR__TOPIC_TYPE);
 		createEAttribute(eventMediatorEClass, EVENT_MEDIATOR__STATIC_TOPIC);
 		createEReference(eventMediatorEClass, EVENT_MEDIATOR__DYNAMIC_TOPIC);
 		createEReference(eventMediatorEClass, EVENT_MEDIATOR__EVENT_EXPRESSION);
 		createEReference(eventMediatorEClass, EVENT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(eventMediatorEClass, EVENT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		eventMediatorInputConnectorEClass = createEClass(EVENT_MEDIATOR_INPUT_CONNECTOR);
 
 		eventMediatorOutputConnectorEClass = createEClass(EVENT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		abstractNameValuePropertyEClass = createEClass(ABSTRACT_NAME_VALUE_PROPERTY);
 		createEAttribute(abstractNameValuePropertyEClass, ABSTRACT_NAME_VALUE_PROPERTY__PROPERTY_NAME);
 		createEAttribute(abstractNameValuePropertyEClass, ABSTRACT_NAME_VALUE_PROPERTY__PROPERTY_VALUE);
 
 		entitlementMediatorEClass = createEClass(ENTITLEMENT_MEDIATOR);
 		createEAttribute(entitlementMediatorEClass, ENTITLEMENT_MEDIATOR__SERVER_URL);
 		createEAttribute(entitlementMediatorEClass, ENTITLEMENT_MEDIATOR__USERNAME);
 		createEAttribute(entitlementMediatorEClass, ENTITLEMENT_MEDIATOR__PASSWORD);
 		createEReference(entitlementMediatorEClass, ENTITLEMENT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(entitlementMediatorEClass, ENTITLEMENT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		entitlementMediatorInputConnectorEClass = createEClass(ENTITLEMENT_MEDIATOR_INPUT_CONNECTOR);
 
 		entitlementMediatorOutputConnectorEClass = createEClass(ENTITLEMENT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		enqueueMediatorEClass = createEClass(ENQUEUE_MEDIATOR);
 		createEAttribute(enqueueMediatorEClass, ENQUEUE_MEDIATOR__EXECUTOR);
 		createEAttribute(enqueueMediatorEClass, ENQUEUE_MEDIATOR__PRIORITY);
 		createEReference(enqueueMediatorEClass, ENQUEUE_MEDIATOR__SEQUENCE_KEY);
 		createEReference(enqueueMediatorEClass, ENQUEUE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(enqueueMediatorEClass, ENQUEUE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		enqueueMediatorInputConnectorEClass = createEClass(ENQUEUE_MEDIATOR_INPUT_CONNECTOR);
 
 		enqueueMediatorOutputConnectorEClass = createEClass(ENQUEUE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		classMediatorEClass = createEClass(CLASS_MEDIATOR);
 		createEAttribute(classMediatorEClass, CLASS_MEDIATOR__CLASS_NAME);
 		createEReference(classMediatorEClass, CLASS_MEDIATOR__PROPERTIES);
 		createEReference(classMediatorEClass, CLASS_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(classMediatorEClass, CLASS_MEDIATOR__OUTPUT_CONNECTOR);
 
 		classMediatorInputConnectorEClass = createEClass(CLASS_MEDIATOR_INPUT_CONNECTOR);
 
 		classMediatorOutputConnectorEClass = createEClass(CLASS_MEDIATOR_OUTPUT_CONNECTOR);
 
 		classPropertyEClass = createEClass(CLASS_PROPERTY);
 
 		springMediatorEClass = createEClass(SPRING_MEDIATOR);
 		createEAttribute(springMediatorEClass, SPRING_MEDIATOR__BEAN_NAME);
 		createEReference(springMediatorEClass, SPRING_MEDIATOR__CONFIGURATION_KEY);
 		createEReference(springMediatorEClass, SPRING_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(springMediatorEClass, SPRING_MEDIATOR__OUTPUT_CONNECTOR);
 
 		springMediatorInputConnectorEClass = createEClass(SPRING_MEDIATOR_INPUT_CONNECTOR);
 
 		springMediatorOutputConnectorEClass = createEClass(SPRING_MEDIATOR_OUTPUT_CONNECTOR);
 
 		scriptMediatorEClass = createEClass(SCRIPT_MEDIATOR);
 		createEAttribute(scriptMediatorEClass, SCRIPT_MEDIATOR__SCRIPT_TYPE);
 		createEAttribute(scriptMediatorEClass, SCRIPT_MEDIATOR__SCRIPT_LANGUAGE);
 		createEAttribute(scriptMediatorEClass, SCRIPT_MEDIATOR__MEDIATE_FUNCTION);
 		createEReference(scriptMediatorEClass, SCRIPT_MEDIATOR__SCRIPT_KEY);
 		createEAttribute(scriptMediatorEClass, SCRIPT_MEDIATOR__SCRIPT_BODY);
 		createEReference(scriptMediatorEClass, SCRIPT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(scriptMediatorEClass, SCRIPT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		scriptMediatorInputConnectorEClass = createEClass(SCRIPT_MEDIATOR_INPUT_CONNECTOR);
 
 		scriptMediatorOutputConnectorEClass = createEClass(SCRIPT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		faultMediatorEClass = createEClass(FAULT_MEDIATOR);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__SOAP_VERSION);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__MARK_AS_RESPONSE);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_CODE_SOAP11);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_STRING_TYPE);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_STRING_VALUE);
 		createEReference(faultMediatorEClass, FAULT_MEDIATOR__FAULT_STRING_EXPRESSION);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_ACTOR);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_CODE_SOAP12);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_REASON_TYPE);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_REASON_VALUE);
 		createEReference(faultMediatorEClass, FAULT_MEDIATOR__FAULT_REASON_EXPRESSION);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__ROLE_NAME);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__NODE_NAME);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_DETAIL_TYPE);
 		createEAttribute(faultMediatorEClass, FAULT_MEDIATOR__FAULT_DETAIL_VALUE);
 		createEReference(faultMediatorEClass, FAULT_MEDIATOR__FAULT_DETAIL_EXPRESSION);
 		createEReference(faultMediatorEClass, FAULT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(faultMediatorEClass, FAULT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		faultMediatorInputConnectorEClass = createEClass(FAULT_MEDIATOR_INPUT_CONNECTOR);
 
 		faultMediatorOutputConnectorEClass = createEClass(FAULT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		aggregateMediatorEClass = createEClass(AGGREGATE_MEDIATOR);
 		createEAttribute(aggregateMediatorEClass, AGGREGATE_MEDIATOR__AGGREGATE_ID);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__CORRELATION_EXPRESSION);
 		createEAttribute(aggregateMediatorEClass, AGGREGATE_MEDIATOR__COMPLETION_TIMEOUT);
 		createEAttribute(aggregateMediatorEClass, AGGREGATE_MEDIATOR__COMPLETION_MIN_MESSAGES);
 		createEAttribute(aggregateMediatorEClass, AGGREGATE_MEDIATOR__COMPLETION_MAX_MESSAGES);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__ON_COMPLETE_OUTPUT_CONNECTOR);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__MEDIATOR_FLOW);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__AGGREGATION_EXPRESSION);
 		createEAttribute(aggregateMediatorEClass, AGGREGATE_MEDIATOR__SEQUENCE_TYPE);
 		createEReference(aggregateMediatorEClass, AGGREGATE_MEDIATOR__SEQUENCE_KEY);
 
 		aggregateMediatorInputConnectorEClass = createEClass(AGGREGATE_MEDIATOR_INPUT_CONNECTOR);
 
 		aggregateMediatorOutputConnectorEClass = createEClass(AGGREGATE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		aggregateMediatorOnCompleteOutputConnectorEClass = createEClass(AGGREGATE_MEDIATOR_ON_COMPLETE_OUTPUT_CONNECTOR);
 
 		routerMediatorEClass = createEClass(ROUTER_MEDIATOR);
 		createEAttribute(routerMediatorEClass, ROUTER_MEDIATOR__CONTINUE_AFTER_ROUTING);
 		createEReference(routerMediatorEClass, ROUTER_MEDIATOR__TARGET_OUTPUT_CONNECTOR);
 		createEReference(routerMediatorEClass, ROUTER_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(routerMediatorEClass, ROUTER_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(routerMediatorEClass, ROUTER_MEDIATOR__ROUTER_CONTAINER);
 
 		routerRouteEClass = createEClass(ROUTER_ROUTE);
 		createEAttribute(routerRouteEClass, ROUTER_ROUTE__BREAK_AFTER_ROUTE);
 		createEReference(routerRouteEClass, ROUTER_ROUTE__ROUTE_EXPRESSION);
 		createEAttribute(routerRouteEClass, ROUTER_ROUTE__ROUTE_PATTERN);
 
 		routerTargetEClass = createEClass(ROUTER_TARGET);
 
 		routerMediatorInputConnectorEClass = createEClass(ROUTER_MEDIATOR_INPUT_CONNECTOR);
 
 		routerMediatorOutputConnectorEClass = createEClass(ROUTER_MEDIATOR_OUTPUT_CONNECTOR);
 
 		routerMediatorTargetOutputConnectorEClass = createEClass(ROUTER_MEDIATOR_TARGET_OUTPUT_CONNECTOR);
 		createEAttribute(routerMediatorTargetOutputConnectorEClass, ROUTER_MEDIATOR_TARGET_OUTPUT_CONNECTOR__SOAP_ACTION);
 		createEAttribute(routerMediatorTargetOutputConnectorEClass, ROUTER_MEDIATOR_TARGET_OUTPUT_CONNECTOR__TO_ADDRESS);
 
 		routerMediatorContainerEClass = createEClass(ROUTER_MEDIATOR_CONTAINER);
 		createEReference(routerMediatorContainerEClass, ROUTER_MEDIATOR_CONTAINER__ROUTER_TARGET_CONTAINER);
 
 		routerTargetContainerEClass = createEClass(ROUTER_TARGET_CONTAINER);
 		createEReference(routerTargetContainerEClass, ROUTER_TARGET_CONTAINER__MEDIATOR_FLOW);
 		createEAttribute(routerTargetContainerEClass, ROUTER_TARGET_CONTAINER__BREAK_AFTER_ROUTE);
 		createEReference(routerTargetContainerEClass, ROUTER_TARGET_CONTAINER__ROUTE_EXPRESSION);
 		createEAttribute(routerTargetContainerEClass, ROUTER_TARGET_CONTAINER__ROUTE_PATTERN);
 		createEReference(routerTargetContainerEClass, ROUTER_TARGET_CONTAINER__TARGET);
 
 		cloneMediatorEClass = createEClass(CLONE_MEDIATOR);
 		createEAttribute(cloneMediatorEClass, CLONE_MEDIATOR__CLONE_ID);
 		createEAttribute(cloneMediatorEClass, CLONE_MEDIATOR__SEQUENTIAL_MEDIATION);
 		createEAttribute(cloneMediatorEClass, CLONE_MEDIATOR__CONTINUE_PARENT);
 		createEReference(cloneMediatorEClass, CLONE_MEDIATOR__TARGETS);
 		createEReference(cloneMediatorEClass, CLONE_MEDIATOR__TARGETS_OUTPUT_CONNECTOR);
 		createEReference(cloneMediatorEClass, CLONE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(cloneMediatorEClass, CLONE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(cloneMediatorEClass, CLONE_MEDIATOR__CLONE_CONTAINER);
 
 		cloneTargetEClass = createEClass(CLONE_TARGET);
 		createEAttribute(cloneTargetEClass, CLONE_TARGET__SOAP_ACTION);
 		createEAttribute(cloneTargetEClass, CLONE_TARGET__TO_ADDRESS);
 
 		cloneMediatorInputConnectorEClass = createEClass(CLONE_MEDIATOR_INPUT_CONNECTOR);
 
 		cloneMediatorOutputConnectorEClass = createEClass(CLONE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		cloneMediatorTargetOutputConnectorEClass = createEClass(CLONE_MEDIATOR_TARGET_OUTPUT_CONNECTOR);
 		createEAttribute(cloneMediatorTargetOutputConnectorEClass, CLONE_MEDIATOR_TARGET_OUTPUT_CONNECTOR__SOAP_ACTION);
 		createEAttribute(cloneMediatorTargetOutputConnectorEClass, CLONE_MEDIATOR_TARGET_OUTPUT_CONNECTOR__TO_ADDRESS);
 
 		cloneMediatorContainerEClass = createEClass(CLONE_MEDIATOR_CONTAINER);
 		createEReference(cloneMediatorContainerEClass, CLONE_MEDIATOR_CONTAINER__CLONE_TARGET_CONTAINER);
 
 		cloneTargetContainerEClass = createEClass(CLONE_TARGET_CONTAINER);
 		createEReference(cloneTargetContainerEClass, CLONE_TARGET_CONTAINER__MEDIATOR_FLOW);
 
 		iterateMediatorEClass = createEClass(ITERATE_MEDIATOR);
 		createEAttribute(iterateMediatorEClass, ITERATE_MEDIATOR__ITERATE_ID);
 		createEAttribute(iterateMediatorEClass, ITERATE_MEDIATOR__SEQUENTIAL_MEDIATION);
 		createEAttribute(iterateMediatorEClass, ITERATE_MEDIATOR__CONTINUE_PARENT);
 		createEAttribute(iterateMediatorEClass, ITERATE_MEDIATOR__PRESERVE_PAYLOAD);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__ITERATE_EXPRESSION);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__ATTACH_PATH);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__TARGET);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__TARGET_OUTPUT_CONNECTOR);
 		createEReference(iterateMediatorEClass, ITERATE_MEDIATOR__MEDIATOR_FLOW);
 
 		iterateMediatorInputConnectorEClass = createEClass(ITERATE_MEDIATOR_INPUT_CONNECTOR);
 
 		iterateMediatorOutputConnectorEClass = createEClass(ITERATE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		iterateMediatorTargetOutputConnectorEClass = createEClass(ITERATE_MEDIATOR_TARGET_OUTPUT_CONNECTOR);
 
 		iterateTargetEClass = createEClass(ITERATE_TARGET);
 		createEAttribute(iterateTargetEClass, ITERATE_TARGET__SOAP_ACTION);
 		createEAttribute(iterateTargetEClass, ITERATE_TARGET__TO_ADDRESS);
 
 		abstractCommonTargetEClass = createEClass(ABSTRACT_COMMON_TARGET);
 		createEAttribute(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__SEQUENCE_TYPE);
 		createEReference(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__SEQUENCE);
 		createEReference(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__SEQUENCE_KEY);
 		createEAttribute(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__ENDPOINT_TYPE);
 		createEReference(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__ENDPOINT);
 		createEReference(abstractCommonTargetEClass, ABSTRACT_COMMON_TARGET__ENDPOINT_KEY);
 
 		mediatorSequenceEClass = createEClass(MEDIATOR_SEQUENCE);
 		createEAttribute(mediatorSequenceEClass, MEDIATOR_SEQUENCE__ANONYMOUS);
 		createEAttribute(mediatorSequenceEClass, MEDIATOR_SEQUENCE__SEQUENCE_NAME);
 		createEReference(mediatorSequenceEClass, MEDIATOR_SEQUENCE__MEDIATORS);
 		createEReference(mediatorSequenceEClass, MEDIATOR_SEQUENCE__ON_ERROR);
 		createEAttribute(mediatorSequenceEClass, MEDIATOR_SEQUENCE__DESCRIPTION);
 
 		cacheMediatorEClass = createEClass(CACHE_MEDIATOR);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__CACHE_ID);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__CACHE_SCOPE);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__CACHE_ACTION);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__HASH_GENERATOR);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__CACHE_TIMEOUT);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__MAX_MESSAGE_SIZE);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__IMPLEMENTATION_TYPE);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__MAX_ENTRY_COUNT);
 		createEAttribute(cacheMediatorEClass, CACHE_MEDIATOR__SEQUENCE_TYPE);
 		createEReference(cacheMediatorEClass, CACHE_MEDIATOR__SEQUENCE_KEY);
 		createEReference(cacheMediatorEClass, CACHE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(cacheMediatorEClass, CACHE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(cacheMediatorEClass, CACHE_MEDIATOR__ON_HIT_OUTPUT_CONNECTOR);
 		createEReference(cacheMediatorEClass, CACHE_MEDIATOR__MEDIATOR_FLOW);
 
 		cacheMediatorInputConnectorEClass = createEClass(CACHE_MEDIATOR_INPUT_CONNECTOR);
 
 		cacheMediatorOutputConnectorEClass = createEClass(CACHE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		cacheMediatorOnHitOutputConnectorEClass = createEClass(CACHE_MEDIATOR_ON_HIT_OUTPUT_CONNECTOR);
 
 		cacheOnHitBranchEClass = createEClass(CACHE_ON_HIT_BRANCH);
 
 		xQueryMediatorEClass = createEClass(XQUERY_MEDIATOR);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__VARIABLES);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__TARGET_XPATH);
 		createEAttribute(xQueryMediatorEClass, XQUERY_MEDIATOR__SCRIPT_KEY_TYPE);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__STATIC_SCRIPT_KEY);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__DYNAMIC_SCRIPT_KEY);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__QUERY_KEY);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(xQueryMediatorEClass, XQUERY_MEDIATOR__OUTPUT_CONNECTOR);
 
 		xQueryMediatorInputConnectorEClass = createEClass(XQUERY_MEDIATOR_INPUT_CONNECTOR);
 
 		xQueryMediatorOutputConnectorEClass = createEClass(XQUERY_MEDIATOR_OUTPUT_CONNECTOR);
 
 		xQueryVariableEClass = createEClass(XQUERY_VARIABLE);
 		createEAttribute(xQueryVariableEClass, XQUERY_VARIABLE__VARIABLE_NAME);
 		createEAttribute(xQueryVariableEClass, XQUERY_VARIABLE__VARIABLE_TYPE);
 		createEAttribute(xQueryVariableEClass, XQUERY_VARIABLE__VALUE_TYPE);
 		createEAttribute(xQueryVariableEClass, XQUERY_VARIABLE__VALUE_LITERAL);
 		createEReference(xQueryVariableEClass, XQUERY_VARIABLE__VALUE_EXPRESSION);
 		createEReference(xQueryVariableEClass, XQUERY_VARIABLE__VALUE_KEY);
 
 		calloutMediatorEClass = createEClass(CALLOUT_MEDIATOR);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__SERVICE_URL);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__SOAP_ACTION);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__PATH_TO_AXIS2XML);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__PATH_TO_AXIS2_REPOSITORY);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__PAYLOAD_TYPE);
 		createEReference(calloutMediatorEClass, CALLOUT_MEDIATOR__PAYLOAD_MESSAGE_XPATH);
 		createEReference(calloutMediatorEClass, CALLOUT_MEDIATOR__PAYLOAD_REGISTRY_KEY);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__RESULT_TYPE);
 		createEReference(calloutMediatorEClass, CALLOUT_MEDIATOR__RESULT_MESSAGE_XPATH);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__RESULT_CONTEXT_PROPERTY);
 		createEAttribute(calloutMediatorEClass, CALLOUT_MEDIATOR__PASS_HEADERS);
 		createEReference(calloutMediatorEClass, CALLOUT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(calloutMediatorEClass, CALLOUT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		calloutMediatorInputConnectorEClass = createEClass(CALLOUT_MEDIATOR_INPUT_CONNECTOR);
 
 		calloutMediatorOutputConnectorEClass = createEClass(CALLOUT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		rmSequenceMediatorEClass = createEClass(RM_SEQUENCE_MEDIATOR);
 		createEAttribute(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__RM_SPEC_VERSION);
 		createEAttribute(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__SEQUENCE_TYPE);
 		createEReference(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__CORRELATION_XPATH);
 		createEReference(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__LAST_MESSAGE_XPATH);
 		createEReference(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(rmSequenceMediatorEClass, RM_SEQUENCE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		rmSequenceMediatorInputConnectorEClass = createEClass(RM_SEQUENCE_MEDIATOR_INPUT_CONNECTOR);
 
 		rmSequenceMediatorOutputConnectorEClass = createEClass(RM_SEQUENCE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		transactionMediatorEClass = createEClass(TRANSACTION_MEDIATOR);
 		createEAttribute(transactionMediatorEClass, TRANSACTION_MEDIATOR__ACTION);
 		createEReference(transactionMediatorEClass, TRANSACTION_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(transactionMediatorEClass, TRANSACTION_MEDIATOR__OUTPUT_CONNECTOR);
 
 		transactionMediatorInputConnectorEClass = createEClass(TRANSACTION_MEDIATOR_INPUT_CONNECTOR);
 
 		transactionMediatorOutputConnectorEClass = createEClass(TRANSACTION_MEDIATOR_OUTPUT_CONNECTOR);
 
 		oAuthMediatorEClass = createEClass(OAUTH_MEDIATOR);
 		createEAttribute(oAuthMediatorEClass, OAUTH_MEDIATOR__REMOTE_SERVICE_URL);
 		createEReference(oAuthMediatorEClass, OAUTH_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(oAuthMediatorEClass, OAUTH_MEDIATOR__OUTPUT_CONNECTOR);
 
 		oAuthMediatorInputConnectorEClass = createEClass(OAUTH_MEDIATOR_INPUT_CONNECTOR);
 
 		oAuthMediatorOutputConnectorEClass = createEClass(OAUTH_MEDIATOR_OUTPUT_CONNECTOR);
 
 		autoscaleInMediatorEClass = createEClass(AUTOSCALE_IN_MEDIATOR);
 
 		autoscaleOutMediatorEClass = createEClass(AUTOSCALE_OUT_MEDIATOR);
 
 		headerMediatorEClass = createEClass(HEADER_MEDIATOR);
 		createEReference(headerMediatorEClass, HEADER_MEDIATOR__HEADER_NAME);
 		createEAttribute(headerMediatorEClass, HEADER_MEDIATOR__HEADER_ACTION);
 		createEAttribute(headerMediatorEClass, HEADER_MEDIATOR__VALUE_TYPE);
 		createEAttribute(headerMediatorEClass, HEADER_MEDIATOR__VALUE_LITERAL);
 		createEReference(headerMediatorEClass, HEADER_MEDIATOR__VALUE_EXPRESSION);
 		createEReference(headerMediatorEClass, HEADER_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(headerMediatorEClass, HEADER_MEDIATOR__OUTPUT_CONNECTOR);
 
 		headerMediatorInputConnectorEClass = createEClass(HEADER_MEDIATOR_INPUT_CONNECTOR);
 
 		headerMediatorOutputConnectorEClass = createEClass(HEADER_MEDIATOR_OUTPUT_CONNECTOR);
 
 		throttleMediatorEClass = createEClass(THROTTLE_MEDIATOR);
 		createEAttribute(throttleMediatorEClass, THROTTLE_MEDIATOR__GROUP_ID);
 		createEAttribute(throttleMediatorEClass, THROTTLE_MEDIATOR__POLICY_TYPE);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__POLICY_KEY);
 		createEAttribute(throttleMediatorEClass, THROTTLE_MEDIATOR__MAX_CONCURRENT_ACCESS_COUNT);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__POLICY_ENTRIES);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__POLICY_CONFIGURATION);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_ACCEPT_BRANCH);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_REJECT_BRANCH);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_ACCEPT_OUTPUT_CONNECTOR);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_REJECT_OUTPUT_CONNECTOR);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__THROTTLE_CONTAINER);
 		createEAttribute(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_ACCEPT_BRANCHSEQUENCE_TYPE);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_ACCEPT_BRANCHSEQUENCE_KEY);
 		createEAttribute(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_REJECT_BRANCHSEQUENCE_TYPE);
 		createEReference(throttleMediatorEClass, THROTTLE_MEDIATOR__ON_REJECT_BRANCHSEQUENCE_KEY);
 
 		throttleMediatorInputConnectorEClass = createEClass(THROTTLE_MEDIATOR_INPUT_CONNECTOR);
 
 		throttleMediatorOutputConnectorEClass = createEClass(THROTTLE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		throttleMediatorOnAcceptOutputConnectorEClass = createEClass(THROTTLE_MEDIATOR_ON_ACCEPT_OUTPUT_CONNECTOR);
 
 		throttleMediatorOnRejectOutputConnectorEClass = createEClass(THROTTLE_MEDIATOR_ON_REJECT_OUTPUT_CONNECTOR);
 
 		throttlePolicyConfigurationEClass = createEClass(THROTTLE_POLICY_CONFIGURATION);
 		createEAttribute(throttlePolicyConfigurationEClass, THROTTLE_POLICY_CONFIGURATION__POLICY_TYPE);
 		createEReference(throttlePolicyConfigurationEClass, THROTTLE_POLICY_CONFIGURATION__POLICY_KEY);
 		createEAttribute(throttlePolicyConfigurationEClass, THROTTLE_POLICY_CONFIGURATION__MAX_CONCURRENT_ACCESS_COUNT);
 		createEReference(throttlePolicyConfigurationEClass, THROTTLE_POLICY_CONFIGURATION__POLICY_ENTRIES);
 
 		throttlePolicyEntryEClass = createEClass(THROTTLE_POLICY_ENTRY);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__THROTTLE_TYPE);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__THROTTLE_RANGE);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__ACCESS_TYPE);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__MAX_REQUEST_COUNT);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__UNIT_TIME);
 		createEAttribute(throttlePolicyEntryEClass, THROTTLE_POLICY_ENTRY__PROHIBIT_PERIOD);
 
 		throttleOnAcceptBranchEClass = createEClass(THROTTLE_ON_ACCEPT_BRANCH);
 		createEAttribute(throttleOnAcceptBranchEClass, THROTTLE_ON_ACCEPT_BRANCH__SEQUENCE_TYPE);
 		createEReference(throttleOnAcceptBranchEClass, THROTTLE_ON_ACCEPT_BRANCH__SEQUENCE_KEY);
 
 		throttleOnRejectBranchEClass = createEClass(THROTTLE_ON_REJECT_BRANCH);
 		createEAttribute(throttleOnRejectBranchEClass, THROTTLE_ON_REJECT_BRANCH__SEQUENCE_TYPE);
 		createEReference(throttleOnRejectBranchEClass, THROTTLE_ON_REJECT_BRANCH__SEQUENCE_KEY);
 
 		throttleContainerEClass = createEClass(THROTTLE_CONTAINER);
 		createEReference(throttleContainerEClass, THROTTLE_CONTAINER__ON_ACCEPT_CONTAINER);
 		createEReference(throttleContainerEClass, THROTTLE_CONTAINER__ON_REJECT_CONTAINER);
 
 		throttleOnAcceptContainerEClass = createEClass(THROTTLE_ON_ACCEPT_CONTAINER);
 		createEReference(throttleOnAcceptContainerEClass, THROTTLE_ON_ACCEPT_CONTAINER__MEDIATOR_FLOW);
 
 		throttleOnRejectContainerEClass = createEClass(THROTTLE_ON_REJECT_CONTAINER);
 		createEReference(throttleOnRejectContainerEClass, THROTTLE_ON_REJECT_CONTAINER__MEDIATOR_FLOW);
 
 		commandMediatorEClass = createEClass(COMMAND_MEDIATOR);
 		createEAttribute(commandMediatorEClass, COMMAND_MEDIATOR__CLASS_NAME);
 		createEReference(commandMediatorEClass, COMMAND_MEDIATOR__PROPERTIES);
 		createEReference(commandMediatorEClass, COMMAND_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(commandMediatorEClass, COMMAND_MEDIATOR__OUTPUT_CONNECTOR);
 
 		commandMediatorInputConnectorEClass = createEClass(COMMAND_MEDIATOR_INPUT_CONNECTOR);
 
 		commandMediatorOutputConnectorEClass = createEClass(COMMAND_MEDIATOR_OUTPUT_CONNECTOR);
 
 		commandPropertyEClass = createEClass(COMMAND_PROPERTY);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__PROPERTY_NAME);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__VALUE_TYPE);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__VALUE_LITERAL);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__VALUE_CONTEXT_PROPERTY_NAME);
 		createEReference(commandPropertyEClass, COMMAND_PROPERTY__VALUE_MESSAGE_ELEMENT_XPATH);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__CONTEXT_ACTION);
 		createEAttribute(commandPropertyEClass, COMMAND_PROPERTY__MESSAGE_ACTION);
 
 		abstractSqlExecutorMediatorEClass = createEClass(ABSTRACT_SQL_EXECUTOR_MEDIATOR);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_TYPE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_DS_TYPE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_DB_DRIVER);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_DS_INITIAL_CONTEXT);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_DS_NAME);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_URL);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_USERNAME);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__CONNECTION_PASSWORD);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_AUTOCOMMIT);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_ISOLATION);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_MAXACTIVE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_MAXIDLE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_MAXOPENSTATEMENTS);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_MAXWAIT);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_MINIDLE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_POOLSTATEMENTS);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_TESTONBORROW);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_TESTWHILEIDLE);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_VALIDATIONQUERY);
 		createEAttribute(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__PROPERTY_INITIALSIZE);
 		createEReference(abstractSqlExecutorMediatorEClass, ABSTRACT_SQL_EXECUTOR_MEDIATOR__SQL_STATEMENTS);
 
 		sqlStatementEClass = createEClass(SQL_STATEMENT);
 		createEAttribute(sqlStatementEClass, SQL_STATEMENT__QUERY_STRING);
 		createEReference(sqlStatementEClass, SQL_STATEMENT__PARAMETERS);
 		createEAttribute(sqlStatementEClass, SQL_STATEMENT__RESULTS_ENABLED);
 		createEReference(sqlStatementEClass, SQL_STATEMENT__RESULTS);
 
 		sqlParameterDefinitionEClass = createEClass(SQL_PARAMETER_DEFINITION);
 		createEAttribute(sqlParameterDefinitionEClass, SQL_PARAMETER_DEFINITION__DATA_TYPE);
 		createEAttribute(sqlParameterDefinitionEClass, SQL_PARAMETER_DEFINITION__VALUE_TYPE);
 		createEAttribute(sqlParameterDefinitionEClass, SQL_PARAMETER_DEFINITION__VALUE_LITERAL);
 		createEReference(sqlParameterDefinitionEClass, SQL_PARAMETER_DEFINITION__VALUE_EXPRESSION);
 
 		sqlResultMappingEClass = createEClass(SQL_RESULT_MAPPING);
 		createEAttribute(sqlResultMappingEClass, SQL_RESULT_MAPPING__PROPERTY_NAME);
 		createEAttribute(sqlResultMappingEClass, SQL_RESULT_MAPPING__COLUMN_ID);
 
 		dbLookupMediatorEClass = createEClass(DB_LOOKUP_MEDIATOR);
 		createEReference(dbLookupMediatorEClass, DB_LOOKUP_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(dbLookupMediatorEClass, DB_LOOKUP_MEDIATOR__OUTPUT_CONNECTOR);
 
 		dbLookupMediatorInputConnectorEClass = createEClass(DB_LOOKUP_MEDIATOR_INPUT_CONNECTOR);
 
 		dbLookupMediatorOutputConnectorEClass = createEClass(DB_LOOKUP_MEDIATOR_OUTPUT_CONNECTOR);
 
 		dbReportMediatorEClass = createEClass(DB_REPORT_MEDIATOR);
 		createEAttribute(dbReportMediatorEClass, DB_REPORT_MEDIATOR__CONNECTION_USE_TRANSACTION);
 		createEReference(dbReportMediatorEClass, DB_REPORT_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(dbReportMediatorEClass, DB_REPORT_MEDIATOR__OUTPUT_CONNECTOR);
 
 		dbReportMediatorInputConnectorEClass = createEClass(DB_REPORT_MEDIATOR_INPUT_CONNECTOR);
 
 		dbReportMediatorOutputConnectorEClass = createEClass(DB_REPORT_MEDIATOR_OUTPUT_CONNECTOR);
 
 		ruleMediatorEClass = createEClass(RULE_MEDIATOR);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__RULE_SET_URI);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__RULE_SET_SOURCE_TYPE);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__RULE_SET_SOURCE_CODE);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__RULE_SET_SOURCE_KEY);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__RULE_SET_PROPERTIES);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__STATEFUL_SESSION);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__RULE_SESSION_PROPERTIES);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__FACTS_CONFIGURATION);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__RESULTS_CONFIGURATION);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__CHILD_MEDIATORS_CONFIGURATION);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__CHILD_MEDIATORS_OUTPUT_CONNECTOR);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__MEDIATOR_FLOW);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__SOURCE_VALUE);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__SOURCE_XPATH);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__TARGET_VALUE);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__TARGET_RESULT_XPATH);
 		createEReference(ruleMediatorEClass, RULE_MEDIATOR__TARGET_XPATH);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__TARGET_ACTION);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__INPUT_WRAPPER_NAME);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__INPUT_NAME_SPACE);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__OUTPUT_WRAPPER_NAME);
 		createEAttribute(ruleMediatorEClass, RULE_MEDIATOR__OUTPUT_NAME_SPACE);
 
 		ruleMediatorInputConnectorEClass = createEClass(RULE_MEDIATOR_INPUT_CONNECTOR);
 
 		ruleMediatorOutputConnectorEClass = createEClass(RULE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		ruleMediatorChildMediatorsOutputConnectorEClass = createEClass(RULE_MEDIATOR_CHILD_MEDIATORS_OUTPUT_CONNECTOR);
 
 		ruleSetCreationPropertyEClass = createEClass(RULE_SET_CREATION_PROPERTY);
 
 		ruleSessionPropertyEClass = createEClass(RULE_SESSION_PROPERTY);
 
 		ruleFactsConfigurationEClass = createEClass(RULE_FACTS_CONFIGURATION);
 		createEReference(ruleFactsConfigurationEClass, RULE_FACTS_CONFIGURATION__FACTS);
 
 		ruleFactEClass = createEClass(RULE_FACT);
 		createEAttribute(ruleFactEClass, RULE_FACT__FACT_TYPE);
 		createEAttribute(ruleFactEClass, RULE_FACT__FACT_CUSTOM_TYPE);
 		createEAttribute(ruleFactEClass, RULE_FACT__FACT_NAME);
 		createEAttribute(ruleFactEClass, RULE_FACT__VALUE_TYPE);
 		createEAttribute(ruleFactEClass, RULE_FACT__VALUE_LITERAL);
 		createEReference(ruleFactEClass, RULE_FACT__VALUE_EXPRESSION);
 		createEReference(ruleFactEClass, RULE_FACT__VALUE_KEY);
 
 		ruleResultsConfigurationEClass = createEClass(RULE_RESULTS_CONFIGURATION);
 		createEReference(ruleResultsConfigurationEClass, RULE_RESULTS_CONFIGURATION__RESULTS);
 
 		ruleResultEClass = createEClass(RULE_RESULT);
 		createEAttribute(ruleResultEClass, RULE_RESULT__RESULT_TYPE);
 		createEAttribute(ruleResultEClass, RULE_RESULT__RESULT_CUSTOM_TYPE);
 		createEAttribute(ruleResultEClass, RULE_RESULT__RESULT_NAME);
 		createEAttribute(ruleResultEClass, RULE_RESULT__VALUE_TYPE);
 		createEAttribute(ruleResultEClass, RULE_RESULT__VALUE_LITERAL);
 		createEReference(ruleResultEClass, RULE_RESULT__VALUE_EXPRESSION);
 		createEReference(ruleResultEClass, RULE_RESULT__VALUE_KEY);
 
 		ruleChildMediatorsConfigurationEClass = createEClass(RULE_CHILD_MEDIATORS_CONFIGURATION);
 
 		callTemplateParameterEClass = createEClass(CALL_TEMPLATE_PARAMETER);
 		createEAttribute(callTemplateParameterEClass, CALL_TEMPLATE_PARAMETER__PARAMETER_NAME);
 		createEAttribute(callTemplateParameterEClass, CALL_TEMPLATE_PARAMETER__TEMPLATE_PARAMETER_TYPE);
 		createEAttribute(callTemplateParameterEClass, CALL_TEMPLATE_PARAMETER__PARAMETER_VALUE);
 		createEReference(callTemplateParameterEClass, CALL_TEMPLATE_PARAMETER__PARAMETER_EXPRESSION);
 
 		callTemplateMediatorEClass = createEClass(CALL_TEMPLATE_MEDIATOR);
 		createEAttribute(callTemplateMediatorEClass, CALL_TEMPLATE_MEDIATOR__TARGET_TEMPLATE);
 		createEReference(callTemplateMediatorEClass, CALL_TEMPLATE_MEDIATOR__TEMPLATE_PARAMETERS);
 		createEReference(callTemplateMediatorEClass, CALL_TEMPLATE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(callTemplateMediatorEClass, CALL_TEMPLATE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		callTemplateMediatorInputConnectorEClass = createEClass(CALL_TEMPLATE_MEDIATOR_INPUT_CONNECTOR);
 
 		callTemplateMediatorOutputConnectorEClass = createEClass(CALL_TEMPLATE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		smooksMediatorEClass = createEClass(SMOOKS_MEDIATOR);
 		createEReference(smooksMediatorEClass, SMOOKS_MEDIATOR__CONFIGURATION_KEY);
 		createEAttribute(smooksMediatorEClass, SMOOKS_MEDIATOR__INPUT_TYPE);
 		createEReference(smooksMediatorEClass, SMOOKS_MEDIATOR__INPUT_EXPRESSION);
 		createEAttribute(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_TYPE);
 		createEReference(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_EXPRESSION);
 		createEAttribute(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_PROPERTY);
 		createEAttribute(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_ACTION);
 		createEAttribute(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_METHOD);
 		createEReference(smooksMediatorEClass, SMOOKS_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(smooksMediatorEClass, SMOOKS_MEDIATOR__OUTPUT_CONNECTOR);
 
 		smooksMediatorInputConnectorEClass = createEClass(SMOOKS_MEDIATOR_INPUT_CONNECTOR);
 
 		smooksMediatorOutputConnectorEClass = createEClass(SMOOKS_MEDIATOR_OUTPUT_CONNECTOR);
 
 		storeMediatorEClass = createEClass(STORE_MEDIATOR);
 		createEAttribute(storeMediatorEClass, STORE_MEDIATOR__MESSAGE_STORE);
 		createEReference(storeMediatorEClass, STORE_MEDIATOR__ON_STORE_SEQUENCE);
 		createEReference(storeMediatorEClass, STORE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(storeMediatorEClass, STORE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		storeMediatorInputConnectorEClass = createEClass(STORE_MEDIATOR_INPUT_CONNECTOR);
 
 		storeMediatorOutputConnectorEClass = createEClass(STORE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		builderMediatorEClass = createEClass(BUILDER_MEDIATOR);
 		createEReference(builderMediatorEClass, BUILDER_MEDIATOR__MESSAGE_BUILDERS);
 		createEReference(builderMediatorEClass, BUILDER_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(builderMediatorEClass, BUILDER_MEDIATOR__OUTPUT_CONNECTOR);
 
 		builderMediatorInputConnectorEClass = createEClass(BUILDER_MEDIATOR_INPUT_CONNECTOR);
 
 		builderMediatorOutputConectorEClass = createEClass(BUILDER_MEDIATOR_OUTPUT_CONECTOR);
 
 		messageBuilderEClass = createEClass(MESSAGE_BUILDER);
 		createEAttribute(messageBuilderEClass, MESSAGE_BUILDER__CONTENT_TYPE);
 		createEAttribute(messageBuilderEClass, MESSAGE_BUILDER__BUILDER_CLASS);
 		createEAttribute(messageBuilderEClass, MESSAGE_BUILDER__FORMATTER_CLASS);
 
 		payloadFactoryMediatorEClass = createEClass(PAYLOAD_FACTORY_MEDIATOR);
 		createEAttribute(payloadFactoryMediatorEClass, PAYLOAD_FACTORY_MEDIATOR__FORMAT);
 		createEReference(payloadFactoryMediatorEClass, PAYLOAD_FACTORY_MEDIATOR__ARGS);
 		createEReference(payloadFactoryMediatorEClass, PAYLOAD_FACTORY_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(payloadFactoryMediatorEClass, PAYLOAD_FACTORY_MEDIATOR__OUTPUT_CONNECTOR);
 
 		payloadFactoryMediatorInputConnectorEClass = createEClass(PAYLOAD_FACTORY_MEDIATOR_INPUT_CONNECTOR);
 
 		payloadFactoryMediatorOutputConnectorEClass = createEClass(PAYLOAD_FACTORY_MEDIATOR_OUTPUT_CONNECTOR);
 
 		payloadFactoryArgumentEClass = createEClass(PAYLOAD_FACTORY_ARGUMENT);
 		createEAttribute(payloadFactoryArgumentEClass, PAYLOAD_FACTORY_ARGUMENT__ARGUMENT_TYPE);
 		createEAttribute(payloadFactoryArgumentEClass, PAYLOAD_FACTORY_ARGUMENT__ARGUMENT_VALUE);
 		createEReference(payloadFactoryArgumentEClass, PAYLOAD_FACTORY_ARGUMENT__ARGUMENT_EXPRESSION);
 
 		conditionalRouteBranchEClass = createEClass(CONDITIONAL_ROUTE_BRANCH);
 		createEAttribute(conditionalRouteBranchEClass, CONDITIONAL_ROUTE_BRANCH__BREAK_AFTER_ROUTE);
 		createEReference(conditionalRouteBranchEClass, CONDITIONAL_ROUTE_BRANCH__EVALUATOR_EXPRESSION);
 		createEReference(conditionalRouteBranchEClass, CONDITIONAL_ROUTE_BRANCH__TARGET_SEQUENCE);
 
 		conditionalRouterMediatorEClass = createEClass(CONDITIONAL_ROUTER_MEDIATOR);
 		createEAttribute(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__CONTINUE_ROUTE);
 		createEReference(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__CONDITIONAL_ROUTE_BRANCHES);
 		createEReference(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__ADDITIONAL_OUTPUT_CONNECTOR);
 		createEReference(conditionalRouterMediatorEClass, CONDITIONAL_ROUTER_MEDIATOR__MEDIATOR_FLOW);
 
 		conditionalRouterMediatorInputConnectorEClass = createEClass(CONDITIONAL_ROUTER_MEDIATOR_INPUT_CONNECTOR);
 
 		conditionalRouterMediatorOutputConnectorEClass = createEClass(CONDITIONAL_ROUTER_MEDIATOR_OUTPUT_CONNECTOR);
 
 		conditionalRouterMediatorAdditionalOutputConnectorEClass = createEClass(CONDITIONAL_ROUTER_MEDIATOR_ADDITIONAL_OUTPUT_CONNECTOR);
 
 		sendMediatorEClass = createEClass(SEND_MEDIATOR);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__END_POINT);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__OUTPUT_CONNECTOR);
 		createEAttribute(sendMediatorEClass, SEND_MEDIATOR__RECEIVING_SEQUENCE_TYPE);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__STATIC_RECEIVING_SEQUENCE);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__DYNAMIC_RECEIVING_SEQUENCE);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__ENDPOINT_OUTPUT_CONNECTOR);
 		createEReference(sendMediatorEClass, SEND_MEDIATOR__ENDPOINT_FLOW);
 
 		sendContainerEClass = createEClass(SEND_CONTAINER);
 		createEReference(sendContainerEClass, SEND_CONTAINER__ENDPOINT_FLOW);
 
 		sendMediatorInputConnectorEClass = createEClass(SEND_MEDIATOR_INPUT_CONNECTOR);
 
 		sendMediatorOutputConnectorEClass = createEClass(SEND_MEDIATOR_OUTPUT_CONNECTOR);
 
 		sendMediatorEndpointOutputConnectorEClass = createEClass(SEND_MEDIATOR_ENDPOINT_OUTPUT_CONNECTOR);
 
 		failoverEndPointEClass = createEClass(FAILOVER_END_POINT);
 		createEReference(failoverEndPointEClass, FAILOVER_END_POINT__INPUT_CONNECTOR);
 		createEReference(failoverEndPointEClass, FAILOVER_END_POINT__OUTPUT_CONNECTOR);
 		createEReference(failoverEndPointEClass, FAILOVER_END_POINT__WEST_OUTPUT_CONNECTOR);
 
 		failoverEndPointInputConnectorEClass = createEClass(FAILOVER_END_POINT_INPUT_CONNECTOR);
 
 		failoverEndPointOutputConnectorEClass = createEClass(FAILOVER_END_POINT_OUTPUT_CONNECTOR);
 
 		failoverEndPointWestOutputConnectorEClass = createEClass(FAILOVER_END_POINT_WEST_OUTPUT_CONNECTOR);
 
 		parentEndPointEClass = createEClass(PARENT_END_POINT);
 		createEReference(parentEndPointEClass, PARENT_END_POINT__CHILDREN);
 
 		wsdlEndPointEClass = createEClass(WSDL_END_POINT);
 		createEReference(wsdlEndPointEClass, WSDL_END_POINT__WSDL_DEFINITION);
 		createEReference(wsdlEndPointEClass, WSDL_END_POINT__WSDL_DESCRIPTION);
 		createEReference(wsdlEndPointEClass, WSDL_END_POINT__INPUT_CONNECTOR);
 		createEReference(wsdlEndPointEClass, WSDL_END_POINT__OUTPUT_CONNECTOR);
 		createEAttribute(wsdlEndPointEClass, WSDL_END_POINT__WSDL_URI);
 		createEAttribute(wsdlEndPointEClass, WSDL_END_POINT__SERVICE);
 		createEAttribute(wsdlEndPointEClass, WSDL_END_POINT__PORT);
 
 		wsdlDefinitionEClass = createEClass(WSDL_DEFINITION);
 
 		wsdlDescriptionEClass = createEClass(WSDL_DESCRIPTION);
 
 		wsdlEndPointInputConnectorEClass = createEClass(WSDL_END_POINT_INPUT_CONNECTOR);
 
 		wsdlEndPointOutputConnectorEClass = createEClass(WSDL_END_POINT_OUTPUT_CONNECTOR);
 
 		loadBalanceEndPointEClass = createEClass(LOAD_BALANCE_END_POINT);
 		createEReference(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__SESSION);
 		createEAttribute(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__FAILOVER);
 		createEAttribute(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__POLICY);
 		createEReference(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__INPUT_CONNECTOR);
 		createEReference(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__OUTPUT_CONNECTOR);
 		createEReference(loadBalanceEndPointEClass, LOAD_BALANCE_END_POINT__WEST_OUTPUT_CONNECTOR);
 
 		loadBalanceEndPointInputConnectorEClass = createEClass(LOAD_BALANCE_END_POINT_INPUT_CONNECTOR);
 
 		loadBalanceEndPointOutputConnectorEClass = createEClass(LOAD_BALANCE_END_POINT_OUTPUT_CONNECTOR);
 
 		loadBalanceEndPointWestOutputConnectorEClass = createEClass(LOAD_BALANCE_END_POINT_WEST_OUTPUT_CONNECTOR);
 
 		localEntryEClass = createEClass(LOCAL_ENTRY);
 		createEAttribute(localEntryEClass, LOCAL_ENTRY__ENTRY_NAME);
 		createEAttribute(localEntryEClass, LOCAL_ENTRY__VALUE_TYPE);
 		createEAttribute(localEntryEClass, LOCAL_ENTRY__VALUE_LITERAL);
 		createEAttribute(localEntryEClass, LOCAL_ENTRY__VALUE_XML);
 		createEAttribute(localEntryEClass, LOCAL_ENTRY__VALUE_URL);
 
 		sessionEClass = createEClass(SESSION);
 		createEAttribute(sessionEClass, SESSION__TYPE);
 
 		sequencesEClass = createEClass(SEQUENCES);
 		createEReference(sequencesEClass, SEQUENCES__OUTPUT_CONNECTOR);
 		createEReference(sequencesEClass, SEQUENCES__INPUT_CONNECTOR);
 		createEReference(sequencesEClass, SEQUENCES__MEDIATOR_FLOW);
 		createEAttribute(sequencesEClass, SEQUENCES__NAME);
 
 		sequencesOutputConnectorEClass = createEClass(SEQUENCES_OUTPUT_CONNECTOR);
 
 		sequencesInputConnectorEClass = createEClass(SEQUENCES_INPUT_CONNECTOR);
 
 		urlRewriteRuleActionEClass = createEClass(URL_REWRITE_RULE_ACTION);
 		createEAttribute(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__RULE_ACTION);
 		createEAttribute(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__RULE_FRAGMENT);
 		createEAttribute(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__RULE_OPTION);
 		createEReference(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__ACTION_EXPRESSION);
 		createEAttribute(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__ACTION_VALUE);
 		createEAttribute(urlRewriteRuleActionEClass, URL_REWRITE_RULE_ACTION__ACTION_REGEX);
 
 		urlRewriteRuleEClass = createEClass(URL_REWRITE_RULE);
 		createEReference(urlRewriteRuleEClass, URL_REWRITE_RULE__URL_REWRITE_RULE_CONDITION);
 		createEReference(urlRewriteRuleEClass, URL_REWRITE_RULE__REWRITE_RULE_ACTION);
 
 		urlRewriteMediatorEClass = createEClass(URL_REWRITE_MEDIATOR);
 		createEReference(urlRewriteMediatorEClass, URL_REWRITE_MEDIATOR__URL_REWRITE_RULES);
 		createEAttribute(urlRewriteMediatorEClass, URL_REWRITE_MEDIATOR__IN_PROPERTY);
 		createEAttribute(urlRewriteMediatorEClass, URL_REWRITE_MEDIATOR__OUT_PROPERTY);
 		createEReference(urlRewriteMediatorEClass, URL_REWRITE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(urlRewriteMediatorEClass, URL_REWRITE_MEDIATOR__OUTPUT_CONNECTOR);
 
 		urlRewriteMediatorInputConnectorEClass = createEClass(URL_REWRITE_MEDIATOR_INPUT_CONNECTOR);
 
 		urlRewriteMediatorOutputConnectorEClass = createEClass(URL_REWRITE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		evaluatorExpressionPropertyEClass = createEClass(EVALUATOR_EXPRESSION_PROPERTY);
 		createEAttribute(evaluatorExpressionPropertyEClass, EVALUATOR_EXPRESSION_PROPERTY__PRETTY_NAME);
 		createEAttribute(evaluatorExpressionPropertyEClass, EVALUATOR_EXPRESSION_PROPERTY__EVALUATOR_NAME);
 		createEAttribute(evaluatorExpressionPropertyEClass, EVALUATOR_EXPRESSION_PROPERTY__EVALUATOR_VALUE);
 
 		validateMediatorEClass = createEClass(VALIDATE_MEDIATOR);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__SOURCE_XPATH);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__FEATURES);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__SCHEMAS);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__INPUT_CONNECTOR);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__OUTPUT_CONNECTOR);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__ON_FAIL_OUTPUT_CONNECTOR);
 		createEReference(validateMediatorEClass, VALIDATE_MEDIATOR__MEDIATOR_FLOW);
 
 		validateFeatureEClass = createEClass(VALIDATE_FEATURE);
 
 		validateSchemaEClass = createEClass(VALIDATE_SCHEMA);
 		createEReference(validateSchemaEClass, VALIDATE_SCHEMA__VALIDATE_STATIC_SCHEMA_KEY);
 		createEReference(validateSchemaEClass, VALIDATE_SCHEMA__VALIDATE_DYNAMIC_SCHEMA_KEY);
 		createEAttribute(validateSchemaEClass, VALIDATE_SCHEMA__VALIDATE_SCHEMA_KEY_TYPE);
 		createEReference(validateSchemaEClass, VALIDATE_SCHEMA__SCHEMA_KEY);
 
 		validateMediatorInputConnectorEClass = createEClass(VALIDATE_MEDIATOR_INPUT_CONNECTOR);
 
 		validateMediatorOutputConnectorEClass = createEClass(VALIDATE_MEDIATOR_OUTPUT_CONNECTOR);
 
 		validateMediatorOnFailOutputConnectorEClass = createEClass(VALIDATE_MEDIATOR_ON_FAIL_OUTPUT_CONNECTOR);
 
 		endpointDiagramEClass = createEClass(ENDPOINT_DIAGRAM);
 		createEReference(endpointDiagramEClass, ENDPOINT_DIAGRAM__CHILD);
 		createEAttribute(endpointDiagramEClass, ENDPOINT_DIAGRAM__NAME);
 
 		namedEndpointEClass = createEClass(NAMED_ENDPOINT);
 		createEReference(namedEndpointEClass, NAMED_ENDPOINT__INPUT_CONNECTOR);
 		createEReference(namedEndpointEClass, NAMED_ENDPOINT__OUTPUT_CONNECTOR);
 		createEAttribute(namedEndpointEClass, NAMED_ENDPOINT__NAME);
 
 		namedEndpointInputConnectorEClass = createEClass(NAMED_ENDPOINT_INPUT_CONNECTOR);
 
 		namedEndpointOutputConnectorEClass = createEClass(NAMED_ENDPOINT_OUTPUT_CONNECTOR);
 
 		templateEClass = createEClass(TEMPLATE);
 		createEAttribute(templateEClass, TEMPLATE__NAME);
 		createEAttribute(templateEClass, TEMPLATE__TEMPLATE_TYPE);
 		createEReference(templateEClass, TEMPLATE__CHILD);
 
 		taskEClass = createEClass(TASK);
 		createEAttribute(taskEClass, TASK__TASK_NAME);
 		createEAttribute(taskEClass, TASK__TASK_GROUP);
 		createEAttribute(taskEClass, TASK__TRIGGER_TYPE);
 		createEAttribute(taskEClass, TASK__COUNT);
 		createEAttribute(taskEClass, TASK__INTERVAL);
 		createEAttribute(taskEClass, TASK__CRON);
 		createEAttribute(taskEClass, TASK__PINNED_SERVERS);
 		createEAttribute(taskEClass, TASK__TASK_IMPLEMENTATION);
 		createEReference(taskEClass, TASK__TASK_PROPERTIES);
 
 		nameValueTypePropertyEClass = createEClass(NAME_VALUE_TYPE_PROPERTY);
 		createEAttribute(nameValueTypePropertyEClass, NAME_VALUE_TYPE_PROPERTY__PROPERTY_NAME);
 		createEAttribute(nameValueTypePropertyEClass, NAME_VALUE_TYPE_PROPERTY__PROPERTY_VALUE);
 		createEAttribute(nameValueTypePropertyEClass, NAME_VALUE_TYPE_PROPERTY__PROPERTY_TYPE);
 
 		taskPropertyEClass = createEClass(TASK_PROPERTY);
 
 		synapseAPIEClass = createEClass(SYNAPSE_API);
 		createEAttribute(synapseAPIEClass, SYNAPSE_API__API_NAME);
 		createEAttribute(synapseAPIEClass, SYNAPSE_API__CONTEXT);
 		createEAttribute(synapseAPIEClass, SYNAPSE_API__HOST_NAME);
 		createEAttribute(synapseAPIEClass, SYNAPSE_API__PORT);
 		createEReference(synapseAPIEClass, SYNAPSE_API__RESOURCES);
 
 		apiResourceEClass = createEClass(API_RESOURCE);
 		createEReference(apiResourceEClass, API_RESOURCE__INPUT_CONNECTOR);
 		createEReference(apiResourceEClass, API_RESOURCE__OUTPUT_CONNECTOR);
 		createEReference(apiResourceEClass, API_RESOURCE__FAULT_INPUT_CONNECTOR);
 		createEAttribute(apiResourceEClass, API_RESOURCE__URL_STYLE);
 		createEAttribute(apiResourceEClass, API_RESOURCE__URI_TEMPLATE);
 		createEAttribute(apiResourceEClass, API_RESOURCE__URL_MAPPING);
 		createEAttribute(apiResourceEClass, API_RESOURCE__ALLOW_GET);
 		createEAttribute(apiResourceEClass, API_RESOURCE__ALLOW_POST);
 		createEAttribute(apiResourceEClass, API_RESOURCE__ALLOW_PUT);
 		createEAttribute(apiResourceEClass, API_RESOURCE__ALLOW_DELETE);
 		createEAttribute(apiResourceEClass, API_RESOURCE__ALLOW_OPTIONS);
 		createEReference(apiResourceEClass, API_RESOURCE__CONTAINER);
 
 		apiResourceInputConnectorEClass = createEClass(API_RESOURCE_INPUT_CONNECTOR);
 
 		apiResourceOutputConnectorEClass = createEClass(API_RESOURCE_OUTPUT_CONNECTOR);
 
 		apiResourceFaultInputConnectorEClass = createEClass(API_RESOURCE_FAULT_INPUT_CONNECTOR);
 
 		apiResourceEndpointEClass = createEClass(API_RESOURCE_ENDPOINT);
 		createEReference(apiResourceEndpointEClass, API_RESOURCE_ENDPOINT__INPUT_CONNECTOR);
 		createEReference(apiResourceEndpointEClass, API_RESOURCE_ENDPOINT__OUTPUT_CONNECTOR);
 
 		apiResourceEndpointInputConnectorEClass = createEClass(API_RESOURCE_ENDPOINT_INPUT_CONNECTOR);
 
 		apiResourceEndpointOutputConnectorEClass = createEClass(API_RESOURCE_ENDPOINT_OUTPUT_CONNECTOR);
 
 		// Create enums
 		artifactTypeEEnum = createEEnum(ARTIFACT_TYPE);
 		sequenceTypeEEnum = createEEnum(SEQUENCE_TYPE);
 		proxyWsdlTypeEEnum = createEEnum(PROXY_WSDL_TYPE);
 		filterConditionTypeEEnum = createEEnum(FILTER_CONDITION_TYPE);
 		logCategoryEEnum = createEEnum(LOG_CATEGORY);
 		logLevelEEnum = createEEnum(LOG_LEVEL);
 		endPointAddressingVersionEEnum = createEEnum(END_POINT_ADDRESSING_VERSION);
 		endPointTimeOutActionEEnum = createEEnum(END_POINT_TIME_OUT_ACTION);
 		endPointMessageFormatEEnum = createEEnum(END_POINT_MESSAGE_FORMAT);
 		endPointAttachmentOptimizationEEnum = createEEnum(END_POINT_ATTACHMENT_OPTIMIZATION);
 		propertyDataTypeEEnum = createEEnum(PROPERTY_DATA_TYPE);
 		propertyActionEEnum = createEEnum(PROPERTY_ACTION);
 		propertyScopeEEnum = createEEnum(PROPERTY_SCOPE);
 		propertyValueTypeEEnum = createEEnum(PROPERTY_VALUE_TYPE);
 		enrichSourceTypeEEnum = createEEnum(ENRICH_SOURCE_TYPE);
 		enrichTargetActionEEnum = createEEnum(ENRICH_TARGET_ACTION);
 		enrichTargetTypeEEnum = createEEnum(ENRICH_TARGET_TYPE);
 		eventTopicTypeEEnum = createEEnum(EVENT_TOPIC_TYPE);
 		scriptTypeEEnum = createEEnum(SCRIPT_TYPE);
 		scriptLanguageEEnum = createEEnum(SCRIPT_LANGUAGE);
 		faultSoapVersionEEnum = createEEnum(FAULT_SOAP_VERSION);
 		faultCodeSoap11EEnum = createEEnum(FAULT_CODE_SOAP11);
 		faultCodeSoap12EEnum = createEEnum(FAULT_CODE_SOAP12);
 		faultStringTypeEEnum = createEEnum(FAULT_STRING_TYPE);
 		faultReasonTypeEEnum = createEEnum(FAULT_REASON_TYPE);
 		faultDetailTypeEEnum = createEEnum(FAULT_DETAIL_TYPE);
 		aggregateSequenceTypeEEnum = createEEnum(AGGREGATE_SEQUENCE_TYPE);
 		targetSequenceTypeEEnum = createEEnum(TARGET_SEQUENCE_TYPE);
 		targetEndpointTypeEEnum = createEEnum(TARGET_ENDPOINT_TYPE);
 		cacheSequenceTypeEEnum = createEEnum(CACHE_SEQUENCE_TYPE);
 		cacheImplementationTypeEEnum = createEEnum(CACHE_IMPLEMENTATION_TYPE);
 		cacheActionEEnum = createEEnum(CACHE_ACTION);
 		cacheScopeEEnum = createEEnum(CACHE_SCOPE);
 		xQueryVariableTypeEEnum = createEEnum(XQUERY_VARIABLE_TYPE);
 		xQueryVariableValueTypeEEnum = createEEnum(XQUERY_VARIABLE_VALUE_TYPE);
 		calloutPayloadTypeEEnum = createEEnum(CALLOUT_PAYLOAD_TYPE);
 		calloutResultTypeEEnum = createEEnum(CALLOUT_RESULT_TYPE);
 		rmSpecVersionEEnum = createEEnum(RM_SPEC_VERSION);
 		rmSequenceTypeEEnum = createEEnum(RM_SEQUENCE_TYPE);
 		transactionActionEEnum = createEEnum(TRANSACTION_ACTION);
 		headerActionEEnum = createEEnum(HEADER_ACTION);
 		headerValueTypeEEnum = createEEnum(HEADER_VALUE_TYPE);
 		throttlePolicyTypeEEnum = createEEnum(THROTTLE_POLICY_TYPE);
 		throttleConditionTypeEEnum = createEEnum(THROTTLE_CONDITION_TYPE);
 		throttleAccessTypeEEnum = createEEnum(THROTTLE_ACCESS_TYPE);
 		throttleSequenceTypeEEnum = createEEnum(THROTTLE_SEQUENCE_TYPE);
 		commandPropertyValueTypeEEnum = createEEnum(COMMAND_PROPERTY_VALUE_TYPE);
 		commandPropertyMessageActionEEnum = createEEnum(COMMAND_PROPERTY_MESSAGE_ACTION);
 		commandPropertyContextActionEEnum = createEEnum(COMMAND_PROPERTY_CONTEXT_ACTION);
 		sqlExecutorConnectionTypeEEnum = createEEnum(SQL_EXECUTOR_CONNECTION_TYPE);
 		sqlExecutorDatasourceTypeEEnum = createEEnum(SQL_EXECUTOR_DATASOURCE_TYPE);
 		sqlExecutorBooleanValueEEnum = createEEnum(SQL_EXECUTOR_BOOLEAN_VALUE);
 		sqlExecutorIsolationLevelEEnum = createEEnum(SQL_EXECUTOR_ISOLATION_LEVEL);
 		sqlParameterValueTypeEEnum = createEEnum(SQL_PARAMETER_VALUE_TYPE);
 		sqlParameterDataTypeEEnum = createEEnum(SQL_PARAMETER_DATA_TYPE);
 		ruleActionsEEnum = createEEnum(RULE_ACTIONS);
 		ruleSourceTypeEEnum = createEEnum(RULE_SOURCE_TYPE);
 		ruleFactTypeEEnum = createEEnum(RULE_FACT_TYPE);
 		ruleFactValueTypeEEnum = createEEnum(RULE_FACT_VALUE_TYPE);
 		ruleResultTypeEEnum = createEEnum(RULE_RESULT_TYPE);
 		ruleResultValueTypeEEnum = createEEnum(RULE_RESULT_VALUE_TYPE);
 		ruleOptionTypeEEnum = createEEnum(RULE_OPTION_TYPE);
 		smooksIODataTypeEEnum = createEEnum(SMOOKS_IO_DATA_TYPE);
 		expressionActionEEnum = createEEnum(EXPRESSION_ACTION);
 		outputMethodEEnum = createEEnum(OUTPUT_METHOD);
 		receivingSequenceTypeEEnum = createEEnum(RECEIVING_SEQUENCE_TYPE);
 		keyTypeEEnum = createEEnum(KEY_TYPE);
 		payloadFactoryArgumentTypeEEnum = createEEnum(PAYLOAD_FACTORY_ARGUMENT_TYPE);
 		typeEEnum = createEEnum(TYPE);
 		localEntryValueTypeEEnum = createEEnum(LOCAL_ENTRY_VALUE_TYPE);
 		ruleActionTypeEEnum = createEEnum(RULE_ACTION_TYPE);
 		ruleFragmentTypeEEnum = createEEnum(RULE_FRAGMENT_TYPE);
 		templateTypeEEnum = createEEnum(TEMPLATE_TYPE);
 		taskPropertyTypeEEnum = createEEnum(TASK_PROPERTY_TYPE);
 		taskTriggerTypeEEnum = createEEnum(TASK_TRIGGER_TYPE);
 		apiResourceUrlStyleEEnum = createEEnum(API_RESOURCE_URL_STYLE);
 
 		// Create data types
 		mapEDataType = createEDataType(MAP);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isInitialized = false;
 
 	/**
 	 * Complete the initialization of the package and its meta-model.  This
 	 * method is guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void initializePackageContents() {
 		if (isInitialized) return;
 		isInitialized = true;
 
 		// Initialize package
 		setName(eNAME);
 		setNsPrefix(eNS_PREFIX);
 		setNsURI(eNS_URI);
 
 		// Create type parameters
 		addETypeParameter(mapEDataType, "K");
 		addETypeParameter(mapEDataType, "V");
 
 		// Set bounds for type parameters
 
 		// Add supertypes to classes
 		esbElementEClass.getESuperTypes().add(this.getEsbNode());
 		esbServerEClass.getESuperTypes().add(this.getEsbNode());
 		mediatorEClass.getESuperTypes().add(this.getEsbElement());
 		inputConnectorEClass.getESuperTypes().add(this.getEsbConnector());
 		outputConnectorEClass.getESuperTypes().add(this.getEsbConnector());
 		additionalOutputConnectorEClass.getESuperTypes().add(this.getEsbConnector());
 		endPointEClass.getESuperTypes().add(this.getEsbElement());
 		proxyServiceEClass.getESuperTypes().add(this.getEsbElement());
 		proxyOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		proxyInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		proxyFaultInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		proxyServiceSequenceAndEndpointContainerEClass.getESuperTypes().add(this.getEsbNode());
 		proxyServiceFaultContainerEClass.getESuperTypes().add(this.getEsbNode());
 		proxyServiceContainerEClass.getESuperTypes().add(this.getEsbNode());
 		mediatorFlowEClass.getESuperTypes().add(this.getEsbNode());
 		endpointFlowEClass.getESuperTypes().add(this.getEsbNode());
 		abstractEndPointEClass.getESuperTypes().add(this.getEndPoint());
 		messageMediatorEClass.getESuperTypes().add(this.getEsbElement());
 		messageInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		messageOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		defaultEndPointEClass.getESuperTypes().add(this.getAbstractEndPoint());
 		defaultEndPointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		defaultEndPointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		addressEndPointEClass.getESuperTypes().add(this.getAbstractEndPoint());
 		addressEndPointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		addressEndPointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		dropMediatorEClass.getESuperTypes().add(this.getMediator());
 		dropMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		filterMediatorEClass.getESuperTypes().add(this.getMediator());
 		filterContainerEClass.getESuperTypes().add(this.getEsbNode());
 		filterPassContainerEClass.getESuperTypes().add(this.getEsbNode());
 		filterFailContainerEClass.getESuperTypes().add(this.getEsbNode());
 		filterMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		filterMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		filterMediatorPassOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		filterMediatorFailOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		mergeNodeEClass.getESuperTypes().add(this.getMediator());
 		mergeNodeFirstInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		mergeNodeSecondInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		mergeNodeOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		logMediatorEClass.getESuperTypes().add(this.getMediator());
 		logMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		logMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		logPropertyEClass.getESuperTypes().add(this.getAbstractNameValueExpressionProperty());
 		propertyMediatorEClass.getESuperTypes().add(this.getMediator());
 		propertyMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		propertyMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		namespacedPropertyEClass.getESuperTypes().add(this.getEsbNode());
 		enrichMediatorEClass.getESuperTypes().add(this.getMediator());
 		enrichMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		enrichMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		abstractNameValueExpressionPropertyEClass.getESuperTypes().add(this.getEsbNode());
 		abstractBooleanFeatureEClass.getESuperTypes().add(this.getEsbNode());
 		abstractLocationKeyResourceEClass.getESuperTypes().add(this.getEsbNode());
 		xsltMediatorEClass.getESuperTypes().add(this.getMediator());
 		xsltPropertyEClass.getESuperTypes().add(this.getAbstractNameValueExpressionProperty());
 		xsltFeatureEClass.getESuperTypes().add(this.getAbstractBooleanFeature());
 		xsltResourceEClass.getESuperTypes().add(this.getAbstractLocationKeyResource());
 		xsltMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		xsltMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		switchMediatorEClass.getESuperTypes().add(this.getMediator());
 		switchCaseBranchOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		switchDefaultBranchOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		switchMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		switchMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		switchMediatorContainerEClass.getESuperTypes().add(this.getEsbNode());
 		switchCaseContainerEClass.getESuperTypes().add(this.getEsbNode());
 		switchDefaultContainerEClass.getESuperTypes().add(this.getEsbNode());
 		esbSequenceEClass.getESuperTypes().add(this.getEsbNode());
 		esbSequenceInputEClass.getESuperTypes().add(this.getEsbNode());
 		esbSequenceOutputEClass.getESuperTypes().add(this.getEsbNode());
 		esbSequenceInputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		esbSequenceOutputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		sequenceEClass.getESuperTypes().add(this.getMediator());
 		sequenceInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		sequenceOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		eventMediatorEClass.getESuperTypes().add(this.getMediator());
 		eventMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		eventMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		entitlementMediatorEClass.getESuperTypes().add(this.getMediator());
 		entitlementMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		entitlementMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		enqueueMediatorEClass.getESuperTypes().add(this.getMediator());
 		enqueueMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		enqueueMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		classMediatorEClass.getESuperTypes().add(this.getMediator());
 		classMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		classMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		classPropertyEClass.getESuperTypes().add(this.getAbstractNameValueProperty());
 		springMediatorEClass.getESuperTypes().add(this.getMediator());
 		springMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		springMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		scriptMediatorEClass.getESuperTypes().add(this.getMediator());
 		scriptMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		scriptMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		faultMediatorEClass.getESuperTypes().add(this.getMediator());
 		faultMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		faultMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		aggregateMediatorEClass.getESuperTypes().add(this.getMediator());
 		aggregateMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		aggregateMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		aggregateMediatorOnCompleteOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		routerMediatorEClass.getESuperTypes().add(this.getMediator());
 		routerRouteEClass.getESuperTypes().add(this.getEsbNode());
 		routerTargetEClass.getESuperTypes().add(this.getAbstractCommonTarget());
 		routerMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		routerMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		routerMediatorTargetOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		routerMediatorContainerEClass.getESuperTypes().add(this.getEsbNode());
 		routerTargetContainerEClass.getESuperTypes().add(this.getEsbNode());
 		cloneMediatorEClass.getESuperTypes().add(this.getMediator());
 		cloneTargetEClass.getESuperTypes().add(this.getAbstractCommonTarget());
 		cloneMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		cloneMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		cloneMediatorTargetOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		cloneMediatorContainerEClass.getESuperTypes().add(this.getEsbNode());
 		cloneTargetContainerEClass.getESuperTypes().add(this.getEsbNode());
 		iterateMediatorEClass.getESuperTypes().add(this.getMediator());
 		iterateMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		iterateMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		iterateMediatorTargetOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		iterateTargetEClass.getESuperTypes().add(this.getAbstractCommonTarget());
 		abstractCommonTargetEClass.getESuperTypes().add(this.getEsbNode());
 		mediatorSequenceEClass.getESuperTypes().add(this.getEsbNode());
 		cacheMediatorEClass.getESuperTypes().add(this.getMediator());
 		cacheMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		cacheMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		cacheMediatorOnHitOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		xQueryMediatorEClass.getESuperTypes().add(this.getMediator());
 		xQueryMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		xQueryMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		calloutMediatorEClass.getESuperTypes().add(this.getMediator());
 		calloutMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		calloutMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		rmSequenceMediatorEClass.getESuperTypes().add(this.getMediator());
 		rmSequenceMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		rmSequenceMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		transactionMediatorEClass.getESuperTypes().add(this.getMediator());
 		transactionMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		transactionMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		oAuthMediatorEClass.getESuperTypes().add(this.getMediator());
 		oAuthMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		oAuthMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		autoscaleInMediatorEClass.getESuperTypes().add(this.getMediator());
 		autoscaleOutMediatorEClass.getESuperTypes().add(this.getMediator());
 		headerMediatorEClass.getESuperTypes().add(this.getMediator());
 		headerMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		headerMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		throttleMediatorEClass.getESuperTypes().add(this.getMediator());
 		throttleMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		throttleMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		throttleMediatorOnAcceptOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		throttleMediatorOnRejectOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		throttleContainerEClass.getESuperTypes().add(this.getEsbNode());
 		throttleOnAcceptContainerEClass.getESuperTypes().add(this.getEsbNode());
 		throttleOnRejectContainerEClass.getESuperTypes().add(this.getEsbNode());
 		commandMediatorEClass.getESuperTypes().add(this.getMediator());
 		commandMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		commandMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		abstractSqlExecutorMediatorEClass.getESuperTypes().add(this.getMediator());
 		dbLookupMediatorEClass.getESuperTypes().add(this.getAbstractSqlExecutorMediator());
 		dbLookupMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		dbLookupMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		dbReportMediatorEClass.getESuperTypes().add(this.getAbstractSqlExecutorMediator());
 		dbReportMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		dbReportMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		ruleMediatorEClass.getESuperTypes().add(this.getMediator());
 		ruleMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		ruleMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		ruleMediatorChildMediatorsOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		ruleSetCreationPropertyEClass.getESuperTypes().add(this.getAbstractNameValueProperty());
 		ruleSessionPropertyEClass.getESuperTypes().add(this.getAbstractNameValueProperty());
 		callTemplateParameterEClass.getESuperTypes().add(this.getEsbNode());
 		callTemplateMediatorEClass.getESuperTypes().add(this.getMediator());
 		callTemplateMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		callTemplateMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		smooksMediatorEClass.getESuperTypes().add(this.getMediator());
 		smooksMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		smooksMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		storeMediatorEClass.getESuperTypes().add(this.getMediator());
 		storeMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		storeMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		builderMediatorEClass.getESuperTypes().add(this.getMediator());
 		builderMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		builderMediatorOutputConectorEClass.getESuperTypes().add(this.getOutputConnector());
 		messageBuilderEClass.getESuperTypes().add(this.getEsbNode());
 		payloadFactoryMediatorEClass.getESuperTypes().add(this.getMediator());
 		payloadFactoryMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		payloadFactoryMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		payloadFactoryArgumentEClass.getESuperTypes().add(this.getEsbNode());
 		conditionalRouteBranchEClass.getESuperTypes().add(this.getEsbNode());
 		conditionalRouterMediatorEClass.getESuperTypes().add(this.getMediator());
 		conditionalRouterMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		conditionalRouterMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		conditionalRouterMediatorAdditionalOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		sendMediatorEClass.getESuperTypes().add(this.getMediator());
 		sendContainerEClass.getESuperTypes().add(this.getEsbNode());
 		sendMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		sendMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		sendMediatorEndpointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		failoverEndPointEClass.getESuperTypes().add(this.getParentEndPoint());
 		failoverEndPointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		failoverEndPointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		failoverEndPointWestOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		parentEndPointEClass.getESuperTypes().add(this.getEndPoint());
 		wsdlEndPointEClass.getESuperTypes().add(this.getAbstractEndPoint());
 		wsdlEndPointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		wsdlEndPointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		loadBalanceEndPointEClass.getESuperTypes().add(this.getParentEndPoint());
 		loadBalanceEndPointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		loadBalanceEndPointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		loadBalanceEndPointWestOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		localEntryEClass.getESuperTypes().add(this.getEsbElement());
 		sequencesEClass.getESuperTypes().add(this.getEsbElement());
 		sequencesOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		sequencesInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		urlRewriteRuleActionEClass.getESuperTypes().add(this.getEsbNode());
 		urlRewriteRuleEClass.getESuperTypes().add(this.getEsbNode());
 		urlRewriteMediatorEClass.getESuperTypes().add(this.getMediator());
 		urlRewriteMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		urlRewriteMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		evaluatorExpressionPropertyEClass.getESuperTypes().add(this.getEsbNode());
 		validateMediatorEClass.getESuperTypes().add(this.getMediator());
 		validateFeatureEClass.getESuperTypes().add(this.getAbstractBooleanFeature());
 		validateSchemaEClass.getESuperTypes().add(this.getEsbNode());
 		validateMediatorInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		validateMediatorOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		validateMediatorOnFailOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		endpointDiagramEClass.getESuperTypes().add(this.getEsbElement());
 		namedEndpointEClass.getESuperTypes().add(this.getAbstractEndPoint());
 		namedEndpointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		namedEndpointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		templateEClass.getESuperTypes().add(this.getEsbElement());
 		taskEClass.getESuperTypes().add(this.getEsbElement());
 		nameValueTypePropertyEClass.getESuperTypes().add(this.getEsbNode());
 		taskPropertyEClass.getESuperTypes().add(this.getNameValueTypeProperty());
 		synapseAPIEClass.getESuperTypes().add(this.getEsbElement());
 		apiResourceEClass.getESuperTypes().add(this.getEsbNode());
 		apiResourceInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		apiResourceOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 		apiResourceFaultInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		apiResourceEndpointEClass.getESuperTypes().add(this.getAbstractEndPoint());
 		apiResourceEndpointInputConnectorEClass.getESuperTypes().add(this.getInputConnector());
 		apiResourceEndpointOutputConnectorEClass.getESuperTypes().add(this.getOutputConnector());
 
 		// Initialize classes and features; add operations and parameters
 		initEClass(esbDiagramEClass, EsbDiagram.class, "EsbDiagram", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEsbDiagram_Server(), this.getEsbServer(), null, "server", null, 0, 1, EsbDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEsbDiagram_Test(), ecorePackage.getEIntegerObject(), "Test", null, 0, 1, EsbDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbNodeEClass, EsbNode.class, "EsbNode", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(esbElementEClass, EsbElement.class, "EsbElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(esbServerEClass, EsbServer.class, "EsbServer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEsbServer_Children(), this.getEsbElement(), null, "children", null, 0, -1, EsbServer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEsbServer_MessageMediator(), this.getMessageMediator(), null, "messageMediator", null, 0, 1, EsbServer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEsbServer_Type(), this.getArtifactType(), "type", null, 0, 1, EsbServer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(mediatorEClass, Mediator.class, "Mediator", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getMediator_Reverse(), ecorePackage.getEBoolean(), "Reverse", "false", 0, 1, Mediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbConnectorEClass, EsbConnector.class, "EsbConnector", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(inputConnectorEClass, InputConnector.class, "InputConnector", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getInputConnector_IncomingLinks(), this.getEsbLink(), this.getEsbLink_Target(), "incomingLinks", null, 0, -1, InputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		EOperation op = addEOperation(inputConnectorEClass, ecorePackage.getEBoolean(), "shouldConnect", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getOutputConnector(), "sourceEnd", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(outputConnectorEClass, OutputConnector.class, "OutputConnector", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getOutputConnector_OutgoingLink(), this.getEsbLink(), this.getEsbLink_Source(), "outgoingLink", null, 0, 1, OutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		op = addEOperation(outputConnectorEClass, ecorePackage.getEBoolean(), "shouldConnect", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getInputConnector(), "targetEnd", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(additionalOutputConnectorEClass, AdditionalOutputConnector.class, "AdditionalOutputConnector", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getAdditionalOutputConnector_AdditionalOutgoingLink(), this.getEsbLink(), null, "additionalOutgoingLink", null, 0, 1, AdditionalOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		op = addEOperation(additionalOutputConnectorEClass, ecorePackage.getEBoolean(), "shouldConnect", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getInputConnector(), "targetEnd", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(esbLinkEClass, EsbLink.class, "EsbLink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEsbLink_Source(), this.getOutputConnector(), this.getOutputConnector_OutgoingLink(), "source", null, 0, 1, EsbLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEsbLink_Target(), this.getInputConnector(), this.getInputConnector_IncomingLinks(), "target", null, 0, 1, EsbLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(endPointEClass, EndPoint.class, "EndPoint", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEndPoint_EndPointName(), ecorePackage.getEString(), "endPointName", null, 0, 1, EndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEndPoint_Anonymous(), ecorePackage.getEBoolean(), "anonymous", "false", 0, 1, EndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyServiceEClass, ProxyService.class, "ProxyService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getProxyService_OutputConnector(), this.getProxyOutputConnector(), null, "outputConnector", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_InputConnector(), this.getProxyInputConnector(), null, "inputConnector", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_FaultInputConnector(), this.getProxyFaultInputConnector(), null, "faultInputConnector", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_Name(), ecorePackage.getEString(), "name", "proxy1", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_PinnedServers(), ecorePackage.getEString(), "pinnedServers", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_ServiceGroup(), ecorePackage.getEString(), "serviceGroup", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_TraceEnabled(), ecorePackage.getEBoolean(), "traceEnabled", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_StatisticsEnabled(), ecorePackage.getEBoolean(), "statisticsEnabled", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_Transports(), ecorePackage.getEString(), "transports", "https,http", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_ReliableMessagingEnabled(), ecorePackage.getEBoolean(), "reliableMessagingEnabled", "false", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_SecurityEnabled(), ecorePackage.getEBoolean(), "securityEnabled", "false", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_WsdlType(), this.getProxyWsdlType(), "wsdlType", "NONE", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_WsdlXML(), ecorePackage.getEString(), "wsdlXML", "<definitions/>", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_WsdlURL(), ecorePackage.getEString(), "wsdlURL", "http://default/wsdl/url", 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_WsdlKey(), this.getRegistryKeyProperty(), null, "wsdlKey", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_ServiceParameters(), this.getProxyServiceParameter(), null, "serviceParameters", null, 0, -1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_ServicePolicies(), this.getProxyServicePolicy(), null, "servicePolicies", null, 0, -1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_Container(), this.getProxyServiceContainer(), null, "container", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_InSequenceType(), this.getSequenceType(), "inSequenceType", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_InSequenceKey(), this.getRegistryKeyProperty(), null, "inSequenceKey", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_InSequenceName(), ecorePackage.getEString(), "inSequenceName", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_OutSequenceType(), this.getSequenceType(), "outSequenceType", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_OutSequenceKey(), this.getRegistryKeyProperty(), null, "outSequenceKey", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_OutSequenceName(), ecorePackage.getEString(), "outSequenceName", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_FaultSequenceType(), this.getSequenceType(), "faultSequenceType", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyService_FaultSequenceKey(), this.getRegistryKeyProperty(), null, "faultSequenceKey", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyService_FaultSequenceName(), ecorePackage.getEString(), "faultSequenceName", null, 0, 1, ProxyService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyOutputConnectorEClass, ProxyOutputConnector.class, "ProxyOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(proxyInputConnectorEClass, ProxyInputConnector.class, "ProxyInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(proxyFaultInputConnectorEClass, ProxyFaultInputConnector.class, "ProxyFaultInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(proxyServiceParameterEClass, ProxyServiceParameter.class, "ProxyServiceParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getProxyServiceParameter_Name(), ecorePackage.getEString(), "name", "parameter_name", 0, 1, ProxyServiceParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProxyServiceParameter_Value(), ecorePackage.getEString(), "value", "parameter_value", 0, 1, ProxyServiceParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyServicePolicyEClass, ProxyServicePolicy.class, "ProxyServicePolicy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getProxyServicePolicy_PolicyKey(), this.getRegistryKeyProperty(), null, "policyKey", null, 0, 1, ProxyServicePolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyServiceSequenceAndEndpointContainerEClass, ProxyServiceSequenceAndEndpointContainer.class, "ProxyServiceSequenceAndEndpointContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getProxyServiceSequenceAndEndpointContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ProxyServiceSequenceAndEndpointContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyServiceFaultContainerEClass, ProxyServiceFaultContainer.class, "ProxyServiceFaultContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getProxyServiceFaultContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ProxyServiceFaultContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(proxyServiceContainerEClass, ProxyServiceContainer.class, "ProxyServiceContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getProxyServiceContainer_SequenceAndEndpointContainer(), this.getProxyServiceSequenceAndEndpointContainer(), null, "sequenceAndEndpointContainer", null, 0, 1, ProxyServiceContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProxyServiceContainer_FaultContainer(), this.getProxyServiceFaultContainer(), null, "faultContainer", null, 0, 1, ProxyServiceContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(mediatorFlowEClass, MediatorFlow.class, "MediatorFlow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getMediatorFlow_Children(), this.getEsbElement(), null, "children", null, 0, -1, MediatorFlow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(endpointFlowEClass, EndpointFlow.class, "EndpointFlow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEndpointFlow_Children(), this.getEsbElement(), null, "children", null, 0, -1, EndpointFlow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractEndPointEClass, AbstractEndPoint.class, "AbstractEndPoint", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractEndPoint_ReliableMessagingEnabled(), ecorePackage.getEBoolean(), "reliableMessagingEnabled", "false", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_SecurityEnabled(), ecorePackage.getEBoolean(), "securityEnabled", "false", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_AddressingEnabled(), ecorePackage.getEBoolean(), "addressingEnabled", "false", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_AddressingVersion(), this.getEndPointAddressingVersion(), "addressingVersion", "final", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_AddressingSeparateListener(), ecorePackage.getEBoolean(), "addressingSeparateListener", "false", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_TimeOutDuration(), ecorePackage.getELong(), "timeOutDuration", "0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_TimeOutAction(), this.getEndPointTimeOutAction(), "timeOutAction", "discard", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_RetryErrorCodes(), ecorePackage.getEString(), "retryErrorCodes", null, 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_RetryCount(), ecorePackage.getEInt(), "retryCount", "0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_RetryDelay(), ecorePackage.getELong(), "retryDelay", "0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_SuspendErrorCodes(), ecorePackage.getEString(), "suspendErrorCodes", null, 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_SuspendInitialDuration(), ecorePackage.getELong(), "suspendInitialDuration", "0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_SuspendMaximumDuration(), ecorePackage.getELong(), "suspendMaximumDuration", "0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractEndPoint_SuspendProgressionFactor(), ecorePackage.getEFloat(), "suspendProgressionFactor", "1.0", 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractEndPoint_ReliableMessagingPolicy(), this.getRegistryKeyProperty(), null, "reliableMessagingPolicy", null, 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractEndPoint_SecurityPolicy(), this.getRegistryKeyProperty(), null, "securityPolicy", null, 0, 1, AbstractEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(messageMediatorEClass, MessageMediator.class, "MessageMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getMessageMediator_InputConnector(), this.getMessageInputConnector(), null, "inputConnector", null, 0, 1, MessageMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMessageMediator_OutputConnector(), this.getMessageOutputConnector(), null, "outputConnector", null, 0, 1, MessageMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(messageInputConnectorEClass, MessageInputConnector.class, "MessageInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(messageOutputConnectorEClass, MessageOutputConnector.class, "MessageOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(defaultEndPointEClass, DefaultEndPoint.class, "DefaultEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getDefaultEndPoint_InputConnector(), this.getDefaultEndPointInputConnector(), null, "inputConnector", null, 0, 1, DefaultEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getDefaultEndPoint_OutputConnector(), this.getDefaultEndPointOutputConnector(), null, "outputConnector", null, 0, 1, DefaultEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(defaultEndPointInputConnectorEClass, DefaultEndPointInputConnector.class, "DefaultEndPointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(defaultEndPointOutputConnectorEClass, DefaultEndPointOutputConnector.class, "DefaultEndPointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(addressEndPointEClass, AddressEndPoint.class, "AddressEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getAddressEndPoint_InputConnector(), this.getAddressEndPointInputConnector(), null, "inputConnector", null, 0, 1, AddressEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAddressEndPoint_OutputConnector(), this.getAddressEndPointOutputConnector(), null, "outputConnector", null, 0, 1, AddressEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAddressEndPoint_URI(), ecorePackage.getEString(), "URI", "http://www.example.org/service", 0, 1, AddressEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(addressEndPointInputConnectorEClass, AddressEndPointInputConnector.class, "AddressEndPointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(addressEndPointOutputConnectorEClass, AddressEndPointOutputConnector.class, "AddressEndPointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(dropMediatorEClass, DropMediator.class, "DropMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getDropMediator_InputConnector(), this.getDropMediatorInputConnector(), null, "inputConnector", null, 0, 1, DropMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(dropMediatorInputConnectorEClass, DropMediatorInputConnector.class, "DropMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(filterMediatorEClass, FilterMediator.class, "FilterMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getFilterMediator_ConditionType(), this.getFilterConditionType(), "conditionType", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFilterMediator_Regex(), ecorePackage.getEString(), "regex", "default_regex", 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_InputConnector(), this.getFilterMediatorInputConnector(), null, "inputConnector", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_OutputConnector(), this.getFilterMediatorOutputConnector(), null, "outputConnector", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_PassOutputConnector(), this.getFilterMediatorPassOutputConnector(), null, "passOutputConnector", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_FailOutputConnector(), this.getFilterMediatorFailOutputConnector(), null, "failOutputConnector", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_Xpath(), this.getNamespacedProperty(), null, "xpath", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_Source(), this.getNamespacedProperty(), null, "source", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterMediator_FilterContainer(), this.getFilterContainer(), null, "filterContainer", null, 0, 1, FilterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(filterContainerEClass, FilterContainer.class, "FilterContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFilterContainer_PassContainer(), this.getFilterPassContainer(), null, "passContainer", null, 0, 1, FilterContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFilterContainer_FailContainer(), this.getFilterFailContainer(), null, "failContainer", null, 0, 1, FilterContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(filterPassContainerEClass, FilterPassContainer.class, "FilterPassContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFilterPassContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, FilterPassContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(filterFailContainerEClass, FilterFailContainer.class, "FilterFailContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFilterFailContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, FilterFailContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(filterMediatorInputConnectorEClass, FilterMediatorInputConnector.class, "FilterMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(filterMediatorOutputConnectorEClass, FilterMediatorOutputConnector.class, "FilterMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(filterMediatorPassOutputConnectorEClass, FilterMediatorPassOutputConnector.class, "FilterMediatorPassOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(filterMediatorFailOutputConnectorEClass, FilterMediatorFailOutputConnector.class, "FilterMediatorFailOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(mergeNodeEClass, MergeNode.class, "MergeNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getMergeNode_FirstInputConnector(), this.getMergeNodeFirstInputConnector(), null, "firstInputConnector", null, 0, 1, MergeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMergeNode_SecondInputConnector(), this.getMergeNodeSecondInputConnector(), null, "secondInputConnector", null, 0, 1, MergeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMergeNode_OutputConnector(), this.getMergeNodeOutputConnector(), null, "outputConnector", null, 0, 1, MergeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(mergeNodeFirstInputConnectorEClass, MergeNodeFirstInputConnector.class, "MergeNodeFirstInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(mergeNodeSecondInputConnectorEClass, MergeNodeSecondInputConnector.class, "MergeNodeSecondInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(mergeNodeOutputConnectorEClass, MergeNodeOutputConnector.class, "MergeNodeOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(logMediatorEClass, LogMediator.class, "LogMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getLogMediator_LogCategory(), this.getLogCategory(), "logCategory", null, 0, 1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLogMediator_LogLevel(), this.getLogLevel(), "logLevel", null, 0, 1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLogMediator_LogSeparator(), ecorePackage.getEString(), "logSeparator", ",", 0, 1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLogMediator_InputConnector(), this.getLogMediatorInputConnector(), null, "inputConnector", null, 0, 1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLogMediator_OutputConnector(), this.getLogMediatorOutputConnector(), null, "outputConnector", null, 0, 1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLogMediator_Properties(), this.getLogProperty(), null, "properties", null, 0, -1, LogMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(logMediatorInputConnectorEClass, LogMediatorInputConnector.class, "LogMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(logMediatorOutputConnectorEClass, LogMediatorOutputConnector.class, "LogMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(logPropertyEClass, LogProperty.class, "LogProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(registryKeyPropertyEClass, RegistryKeyProperty.class, "RegistryKeyProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRegistryKeyProperty_PrettyName(), ecorePackage.getEString(), "prettyName", "Registry Key", 0, 1, RegistryKeyProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRegistryKeyProperty_KeyName(), ecorePackage.getEString(), "keyName", "keyName", 0, 1, RegistryKeyProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRegistryKeyProperty_KeyValue(), ecorePackage.getEString(), "keyValue", null, 0, 1, RegistryKeyProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		EGenericType g1 = createEGenericType(this.getMap());
 		EGenericType g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		initEAttribute(getRegistryKeyProperty_Filters(), g1, "filters", null, 0, 1, RegistryKeyProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(propertyMediatorEClass, PropertyMediator.class, "PropertyMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getPropertyMediator_InputConnector(), this.getPropertyMediatorInputConnector(), null, "inputConnector", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPropertyMediator_OutputConnector(), this.getPropertyMediatorOutputConnector(), null, "outputConnector", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_PropertyName(), ecorePackage.getEString(), "propertyName", "property_name", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_PropertyDataType(), this.getPropertyDataType(), "propertyDataType", "STRING", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_PropertyAction(), this.getPropertyAction(), "propertyAction", "SET", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_PropertyScope(), this.getPropertyScope(), "propertyScope", "SYNAPSE", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_ValueType(), this.getPropertyValueType(), "valueType", "LITERAL", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "value", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_Expression(), ecorePackage.getEString(), "expression", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_NamespacePrefix(), ecorePackage.getEString(), "namespacePrefix", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPropertyMediator_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_ValueOM(), ecorePackage.getEString(), "valueOM", "<value/>", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_ValueStringPattern(), ecorePackage.getEString(), "valueStringPattern", "", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPropertyMediator_ValueStringCapturingGroup(), ecorePackage.getEInt(), "valueStringCapturingGroup", "0", 0, 1, PropertyMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(propertyMediatorInputConnectorEClass, PropertyMediatorInputConnector.class, "PropertyMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(propertyMediatorOutputConnectorEClass, PropertyMediatorOutputConnector.class, "PropertyMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(namespacedPropertyEClass, NamespacedProperty.class, "NamespacedProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getNamespacedProperty_PrettyName(), ecorePackage.getEString(), "prettyName", "Namespaced Property", 0, 1, NamespacedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getNamespacedProperty_PropertyName(), ecorePackage.getEString(), "propertyName", "propertyName", 0, 1, NamespacedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getNamespacedProperty_PropertyValue(), ecorePackage.getEString(), "propertyValue", null, 0, 1, NamespacedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(this.getMap());
 		g2 = createEGenericType(ecorePackage.getEString());
 		g1.getETypeArguments().add(g2);
 		g2 = createEGenericType(ecorePackage.getEString());
 		g1.getETypeArguments().add(g2);
 		initEAttribute(getNamespacedProperty_Namespaces(), g1, "namespaces", null, 0, 1, NamespacedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(enrichMediatorEClass, EnrichMediator.class, "EnrichMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEnrichMediator_CloneSource(), ecorePackage.getEBoolean(), "cloneSource", "false", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_SourceType(), this.getEnrichSourceType(), "sourceType", "CUSTOM", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnrichMediator_SourceXpath(), this.getNamespacedProperty(), null, "sourceXpath", null, 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_SourceProperty(), ecorePackage.getEString(), "sourceProperty", "source_property", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_SourceXML(), ecorePackage.getEString(), "sourceXML", "<inline/>", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_TargetAction(), this.getEnrichTargetAction(), "targetAction", "REPLACE", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_TargetType(), this.getEnrichTargetType(), "targetType", "CUSTOM", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnrichMediator_TargetXpath(), this.getNamespacedProperty(), null, "targetXpath", null, 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnrichMediator_TargetProperty(), ecorePackage.getEString(), "targetProperty", "target_property", 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnrichMediator_InputConnector(), this.getEnrichMediatorInputConnector(), null, "inputConnector", null, 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnrichMediator_OutputConnector(), this.getEnrichMediatorOutputConnector(), null, "outputConnector", null, 0, 1, EnrichMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(enrichMediatorInputConnectorEClass, EnrichMediatorInputConnector.class, "EnrichMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(enrichMediatorOutputConnectorEClass, EnrichMediatorOutputConnector.class, "EnrichMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(abstractNameValueExpressionPropertyEClass, AbstractNameValueExpressionProperty.class, "AbstractNameValueExpressionProperty", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractNameValueExpressionProperty_PropertyName(), ecorePackage.getEString(), "propertyName", "property_name", 0, 1, AbstractNameValueExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractNameValueExpressionProperty_PropertyValueType(), this.getPropertyValueType(), "propertyValueType", "VALUE", 0, 1, AbstractNameValueExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractNameValueExpressionProperty_PropertyValue(), ecorePackage.getEString(), "propertyValue", "property_value", 0, 1, AbstractNameValueExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractNameValueExpressionProperty_PropertyExpression(), this.getNamespacedProperty(), null, "propertyExpression", null, 0, 1, AbstractNameValueExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractBooleanFeatureEClass, AbstractBooleanFeature.class, "AbstractBooleanFeature", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractBooleanFeature_FeatureName(), ecorePackage.getEString(), "featureName", "feature_name", 0, 1, AbstractBooleanFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractBooleanFeature_FeatureEnabled(), ecorePackage.getEBoolean(), "featureEnabled", "true", 0, 1, AbstractBooleanFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractLocationKeyResourceEClass, AbstractLocationKeyResource.class, "AbstractLocationKeyResource", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractLocationKeyResource_Location(), ecorePackage.getEString(), "location", "default_location", 0, 1, AbstractLocationKeyResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractLocationKeyResource_Key(), this.getRegistryKeyProperty(), null, "key", null, 0, 1, AbstractLocationKeyResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(xsltMediatorEClass, XSLTMediator.class, "XSLTMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getXSLTMediator_InputConnector(), this.getXSLTMediatorInputConnector(), null, "inputConnector", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_OutputConnector(), this.getXSLTMediatorOutputConnector(), null, "outputConnector", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getXSLTMediator_XsltSchemaKeyType(), this.getKeyType(), "xsltSchemaKeyType", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_XsltStaticSchemaKey(), this.getRegistryKeyProperty(), null, "xsltStaticSchemaKey", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_XsltDynamicSchemaKey(), this.getNamespacedProperty(), null, "xsltDynamicSchemaKey", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_XsltKey(), this.getRegistryKeyProperty(), null, "xsltKey", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_SourceXPath(), this.getNamespacedProperty(), null, "sourceXPath", null, 0, 1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_Properties(), this.getXSLTProperty(), null, "properties", null, 0, -1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_Features(), this.getXSLTFeature(), null, "features", null, 0, -1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXSLTMediator_Resources(), this.getXSLTResource(), null, "resources", null, 0, -1, XSLTMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(xsltPropertyEClass, XSLTProperty.class, "XSLTProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xsltFeatureEClass, XSLTFeature.class, "XSLTFeature", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xsltResourceEClass, XSLTResource.class, "XSLTResource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xsltMediatorInputConnectorEClass, XSLTMediatorInputConnector.class, "XSLTMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xsltMediatorOutputConnectorEClass, XSLTMediatorOutputConnector.class, "XSLTMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(switchMediatorEClass, SwitchMediator.class, "SwitchMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSwitchMediator_SourceXpath(), this.getNamespacedProperty(), null, "sourceXpath", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSwitchMediator_Source(), ecorePackage.getEString(), "source", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSwitchMediator_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSwitchMediator_NamespacePrefix(), ecorePackage.getEString(), "namespacePrefix", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediator_CaseBranches(), this.getSwitchCaseBranchOutputConnector(), null, "caseBranches", null, 0, -1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediator_DefaultBranch(), this.getSwitchDefaultBranchOutputConnector(), null, "defaultBranch", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediator_InputConnector(), this.getSwitchMediatorInputConnector(), null, "inputConnector", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediator_OutputConnector(), this.getSwitchMediatorOutputConnector(), null, "outputConnector", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediator_SwitchContainer(), this.getSwitchMediatorContainer(), null, "switchContainer", null, 0, 1, SwitchMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(switchCaseBranchOutputConnectorEClass, SwitchCaseBranchOutputConnector.class, "SwitchCaseBranchOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSwitchCaseBranchOutputConnector_CaseRegex(), ecorePackage.getEString(), "caseRegex", ".*+", 0, 1, SwitchCaseBranchOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(switchDefaultBranchOutputConnectorEClass, SwitchDefaultBranchOutputConnector.class, "SwitchDefaultBranchOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(switchMediatorInputConnectorEClass, SwitchMediatorInputConnector.class, "SwitchMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(switchMediatorOutputConnectorEClass, SwitchMediatorOutputConnector.class, "SwitchMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(switchMediatorContainerEClass, SwitchMediatorContainer.class, "SwitchMediatorContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSwitchMediatorContainer_SwitchCaseContainer(), this.getSwitchCaseContainer(), null, "switchCaseContainer", null, 1, -1, SwitchMediatorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSwitchMediatorContainer_SwitchDefaultContainer(), this.getSwitchDefaultContainer(), null, "switchDefaultContainer", null, 0, 1, SwitchMediatorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(switchCaseContainerEClass, SwitchCaseContainer.class, "SwitchCaseContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSwitchCaseContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, SwitchCaseContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(switchDefaultContainerEClass, SwitchDefaultContainer.class, "SwitchDefaultContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSwitchDefaultContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, SwitchDefaultContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sequenceDiagramEClass, SequenceDiagram.class, "SequenceDiagram", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSequenceDiagram_Sequence(), this.getEsbSequence(), null, "sequence", null, 0, 1, SequenceDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbSequenceEClass, EsbSequence.class, "EsbSequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEsbSequence_Name(), ecorePackage.getEString(), "name", ",", 0, 1, EsbSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEsbSequence_Input(), this.getEsbSequenceInputConnector(), null, "input", null, 0, 1, EsbSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEsbSequence_Output(), this.getEsbSequenceOutputConnector(), null, "output", null, 0, 1, EsbSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEsbSequence_ChildMediators(), this.getMediator(), null, "childMediators", null, 0, -1, EsbSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbSequenceInputEClass, EsbSequenceInput.class, "EsbSequenceInput", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEsbSequenceInput_Connector(), this.getEsbSequenceInputConnector(), null, "connector", null, 0, 1, EsbSequenceInput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbSequenceOutputEClass, EsbSequenceOutput.class, "EsbSequenceOutput", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEsbSequenceOutput_Connector(), this.getEsbSequenceOutputConnector(), null, "connector", null, 0, 1, EsbSequenceOutput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(esbSequenceInputConnectorEClass, EsbSequenceInputConnector.class, "EsbSequenceInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(esbSequenceOutputConnectorEClass, EsbSequenceOutputConnector.class, "EsbSequenceOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sequenceEClass, Sequence.class, "Sequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSequence_Name(), ecorePackage.getEString(), "name", "", 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSequence_Key(), ecorePackage.getEString(), "key", "<inline/>", 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSequence_InputConnector(), this.getSequenceInputConnector(), null, "inputConnector", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSequence_OutputConnector(), this.getSequenceOutputConnector(), null, "outputConnector", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSequence_IncludedMediators(), this.getMediator(), null, "includedMediators", null, 0, -1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sequenceInputConnectorEClass, SequenceInputConnector.class, "SequenceInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sequenceOutputConnectorEClass, SequenceOutputConnector.class, "SequenceOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(eventMediatorEClass, EventMediator.class, "EventMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEventMediator_TopicType(), this.getEventTopicType(), "topicType", "CUSTOM", 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEventMediator_StaticTopic(), ecorePackage.getEString(), "staticTopic", "source_property", 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEventMediator_DynamicTopic(), this.getNamespacedProperty(), null, "dynamicTopic", null, 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEventMediator_EventExpression(), this.getNamespacedProperty(), null, "eventExpression", null, 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEventMediator_InputConnector(), this.getEventMediatorInputConnector(), null, "inputConnector", null, 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEventMediator_OutputConnector(), this.getEventMediatorOutputConnector(), null, "outputConnector", null, 0, 1, EventMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(eventMediatorInputConnectorEClass, EventMediatorInputConnector.class, "EventMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(eventMediatorOutputConnectorEClass, EventMediatorOutputConnector.class, "EventMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(abstractNameValuePropertyEClass, AbstractNameValueProperty.class, "AbstractNameValueProperty", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractNameValueProperty_PropertyName(), ecorePackage.getEString(), "propertyName", "property_name", 0, 1, AbstractNameValueProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractNameValueProperty_PropertyValue(), ecorePackage.getEString(), "propertyValue", "property_value", 0, 1, AbstractNameValueProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(entitlementMediatorEClass, EntitlementMediator.class, "EntitlementMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEntitlementMediator_ServerURL(), ecorePackage.getEString(), "serverURL", "server_url", 0, 1, EntitlementMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntitlementMediator_Username(), ecorePackage.getEString(), "username", "username", 0, 1, EntitlementMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntitlementMediator_Password(), ecorePackage.getEString(), "password", "password", 0, 1, EntitlementMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEntitlementMediator_InputConnector(), this.getEntitlementMediatorInputConnector(), null, "inputConnector", null, 0, 1, EntitlementMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEntitlementMediator_OutputConnector(), this.getEntitlementMediatorOutputConnector(), null, "outputConnector", null, 0, 1, EntitlementMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(entitlementMediatorInputConnectorEClass, EntitlementMediatorInputConnector.class, "EntitlementMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(entitlementMediatorOutputConnectorEClass, EntitlementMediatorOutputConnector.class, "EntitlementMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(enqueueMediatorEClass, EnqueueMediator.class, "EnqueueMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEnqueueMediator_Executor(), ecorePackage.getEString(), "executor", "executor_name", 0, 1, EnqueueMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEnqueueMediator_Priority(), ecorePackage.getEInt(), "priority", "0", 0, 1, EnqueueMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnqueueMediator_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, EnqueueMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnqueueMediator_InputConnector(), this.getEnqueueMediatorInputConnector(), null, "inputConnector", null, 0, 1, EnqueueMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getEnqueueMediator_OutputConnector(), this.getEnqueueMediatorOutputConnector(), null, "outputConnector", null, 0, 1, EnqueueMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(enqueueMediatorInputConnectorEClass, EnqueueMediatorInputConnector.class, "EnqueueMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(enqueueMediatorOutputConnectorEClass, EnqueueMediatorOutputConnector.class, "EnqueueMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(classMediatorEClass, ClassMediator.class, "ClassMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getClassMediator_ClassName(), ecorePackage.getEString(), "className", "class_name", 0, 1, ClassMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getClassMediator_Properties(), this.getClassProperty(), null, "properties", null, 0, -1, ClassMediator.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getClassMediator_InputConnector(), this.getClassMediatorInputConnector(), null, "inputConnector", null, 0, 1, ClassMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getClassMediator_OutputConnector(), this.getClassMediatorOutputConnector(), null, "outputConnector", null, 0, 1, ClassMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(classMediatorInputConnectorEClass, ClassMediatorInputConnector.class, "ClassMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(classMediatorOutputConnectorEClass, ClassMediatorOutputConnector.class, "ClassMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(classPropertyEClass, ClassProperty.class, "ClassProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(springMediatorEClass, SpringMediator.class, "SpringMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSpringMediator_BeanName(), ecorePackage.getEString(), "beanName", "bean_name", 0, 1, SpringMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSpringMediator_ConfigurationKey(), this.getRegistryKeyProperty(), null, "configurationKey", null, 0, 1, SpringMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSpringMediator_InputConnector(), this.getSpringMediatorInputConnector(), null, "inputConnector", null, 0, 1, SpringMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSpringMediator_OutputConnector(), this.getSpringMediatorOutputConnector(), null, "outputConnector", null, 0, 1, SpringMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(springMediatorInputConnectorEClass, SpringMediatorInputConnector.class, "SpringMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(springMediatorOutputConnectorEClass, SpringMediatorOutputConnector.class, "SpringMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(scriptMediatorEClass, ScriptMediator.class, "ScriptMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getScriptMediator_ScriptType(), this.getScriptType(), "scriptType", "REGISTRY_REFERENCE", 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getScriptMediator_ScriptLanguage(), this.getScriptLanguage(), "scriptLanguage", "JAVASCRIPT", 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getScriptMediator_MediateFunction(), ecorePackage.getEString(), "mediateFunction", "mediate", 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getScriptMediator_ScriptKey(), this.getRegistryKeyProperty(), null, "scriptKey", null, 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getScriptMediator_ScriptBody(), ecorePackage.getEString(), "scriptBody", "script_code", 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getScriptMediator_InputConnector(), this.getScriptMediatorInputConnector(), null, "inputConnector", null, 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getScriptMediator_OutputConnector(), this.getScriptMediatorOutputConnector(), null, "outputConnector", null, 0, 1, ScriptMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(scriptMediatorInputConnectorEClass, ScriptMediatorInputConnector.class, "ScriptMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(scriptMediatorOutputConnectorEClass, ScriptMediatorOutputConnector.class, "ScriptMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(faultMediatorEClass, FaultMediator.class, "FaultMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getFaultMediator_SoapVersion(), this.getFaultSoapVersion(), "soapVersion", "SOAP_1_1", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_MarkAsResponse(), ecorePackage.getEBoolean(), "markAsResponse", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultCodeSoap11(), this.getFaultCodeSoap11(), "faultCodeSoap11", "VERSION_MISSMATCH", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultStringType(), this.getFaultStringType(), "faultStringType", "VALUE", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultStringValue(), ecorePackage.getEString(), "faultStringValue", "fault_string", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFaultMediator_FaultStringExpression(), this.getNamespacedProperty(), null, "faultStringExpression", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultActor(), ecorePackage.getEString(), "faultActor", "", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultCodeSoap12(), this.getFaultCodeSoap12(), "faultCodeSoap12", "VERSION_MISSMATCH", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultReasonType(), this.getFaultReasonType(), "faultReasonType", "VALUE", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultReasonValue(), ecorePackage.getEString(), "faultReasonValue", "fault_reason", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFaultMediator_FaultReasonExpression(), this.getNamespacedProperty(), null, "faultReasonExpression", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_RoleName(), ecorePackage.getEString(), "roleName", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_NodeName(), ecorePackage.getEString(), "nodeName", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultDetailType(), this.getFaultDetailType(), "faultDetailType", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFaultMediator_FaultDetailValue(), ecorePackage.getEString(), "faultDetailValue", "fault_detail", 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFaultMediator_FaultDetailExpression(), this.getNamespacedProperty(), null, "faultDetailExpression", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFaultMediator_InputConnector(), this.getFaultMediatorInputConnector(), null, "inputConnector", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFaultMediator_OutputConnector(), this.getFaultMediatorOutputConnector(), null, "outputConnector", null, 0, 1, FaultMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(faultMediatorInputConnectorEClass, FaultMediatorInputConnector.class, "FaultMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(faultMediatorOutputConnectorEClass, FaultMediatorOutputConnector.class, "FaultMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(aggregateMediatorEClass, AggregateMediator.class, "AggregateMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAggregateMediator_AggregateID(), ecorePackage.getEString(), "aggregateID", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_CorrelationExpression(), this.getNamespacedProperty(), null, "correlationExpression", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAggregateMediator_CompletionTimeout(), ecorePackage.getEInt(), "completionTimeout", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAggregateMediator_CompletionMinMessages(), ecorePackage.getEInt(), "completionMinMessages", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAggregateMediator_CompletionMaxMessages(), ecorePackage.getEInt(), "completionMaxMessages", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_InputConnector(), this.getAggregateMediatorInputConnector(), null, "inputConnector", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_OutputConnector(), this.getAggregateMediatorOutputConnector(), null, "outputConnector", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_OnCompleteOutputConnector(), this.getAggregateMediatorOnCompleteOutputConnector(), null, "onCompleteOutputConnector", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_AggregationExpression(), this.getNamespacedProperty(), null, "aggregationExpression", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAggregateMediator_SequenceType(), this.getAggregateSequenceType(), "sequenceType", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAggregateMediator_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, AggregateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(aggregateMediatorInputConnectorEClass, AggregateMediatorInputConnector.class, "AggregateMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(aggregateMediatorOutputConnectorEClass, AggregateMediatorOutputConnector.class, "AggregateMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(aggregateMediatorOnCompleteOutputConnectorEClass, AggregateMediatorOnCompleteOutputConnector.class, "AggregateMediatorOnCompleteOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(routerMediatorEClass, RouterMediator.class, "RouterMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRouterMediator_ContinueAfterRouting(), ecorePackage.getEBoolean(), "continueAfterRouting", null, 0, 1, RouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterMediator_TargetOutputConnector(), this.getRouterMediatorTargetOutputConnector(), null, "targetOutputConnector", null, 0, -1, RouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterMediator_InputConnector(), this.getRouterMediatorInputConnector(), null, "inputConnector", null, 0, 1, RouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterMediator_OutputConnector(), this.getRouterMediatorOutputConnector(), null, "outputConnector", null, 0, 1, RouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterMediator_RouterContainer(), this.getRouterMediatorContainer(), null, "routerContainer", null, 0, 1, RouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(routerRouteEClass, RouterRoute.class, "RouterRoute", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRouterRoute_BreakAfterRoute(), ecorePackage.getEBoolean(), "breakAfterRoute", null, 0, 1, RouterRoute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterRoute_RouteExpression(), this.getNamespacedProperty(), null, "routeExpression", null, 0, 1, RouterRoute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRouterRoute_RoutePattern(), ecorePackage.getEString(), "routePattern", null, 0, 1, RouterRoute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(routerTargetEClass, RouterTarget.class, "RouterTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(routerMediatorInputConnectorEClass, RouterMediatorInputConnector.class, "RouterMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(routerMediatorOutputConnectorEClass, RouterMediatorOutputConnector.class, "RouterMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(routerMediatorTargetOutputConnectorEClass, RouterMediatorTargetOutputConnector.class, "RouterMediatorTargetOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRouterMediatorTargetOutputConnector_SoapAction(), ecorePackage.getEString(), "soapAction", "soapAction", 0, 1, RouterMediatorTargetOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRouterMediatorTargetOutputConnector_ToAddress(), ecorePackage.getEString(), "toAddress", "toAddress", 0, 1, RouterMediatorTargetOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(routerMediatorContainerEClass, RouterMediatorContainer.class, "RouterMediatorContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRouterMediatorContainer_RouterTargetContainer(), this.getRouterTargetContainer(), null, "routerTargetContainer", null, 0, -1, RouterMediatorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(routerTargetContainerEClass, RouterTargetContainer.class, "RouterTargetContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRouterTargetContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, RouterTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRouterTargetContainer_BreakAfterRoute(), ecorePackage.getEBoolean(), "breakAfterRoute", null, 0, 1, RouterTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterTargetContainer_RouteExpression(), this.getNamespacedProperty(), null, "routeExpression", null, 0, 1, RouterTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRouterTargetContainer_RoutePattern(), ecorePackage.getEString(), "routePattern", null, 0, 1, RouterTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRouterTargetContainer_Target(), this.getRouterTarget(), null, "Target", null, 0, 1, RouterTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cloneMediatorEClass, CloneMediator.class, "CloneMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCloneMediator_CloneID(), ecorePackage.getEString(), "cloneID", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCloneMediator_SequentialMediation(), ecorePackage.getEBoolean(), "sequentialMediation", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCloneMediator_ContinueParent(), ecorePackage.getEBoolean(), "continueParent", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCloneMediator_Targets(), this.getCloneTarget(), null, "targets", null, 0, -1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCloneMediator_TargetsOutputConnector(), this.getCloneMediatorTargetOutputConnector(), null, "targetsOutputConnector", null, 0, -1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCloneMediator_InputConnector(), this.getCloneMediatorInputConnector(), null, "inputConnector", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCloneMediator_OutputConnector(), this.getCloneMediatorOutputConnector(), null, "outputConnector", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCloneMediator_CloneContainer(), this.getCloneMediatorContainer(), null, "cloneContainer", null, 0, 1, CloneMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cloneTargetEClass, CloneTarget.class, "CloneTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCloneTarget_SoapAction(), ecorePackage.getEString(), "soapAction", "soap_action", 0, 1, CloneTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCloneTarget_ToAddress(), ecorePackage.getEString(), "toAddress", "to_address", 0, 1, CloneTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cloneMediatorInputConnectorEClass, CloneMediatorInputConnector.class, "CloneMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(cloneMediatorOutputConnectorEClass, CloneMediatorOutputConnector.class, "CloneMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(cloneMediatorTargetOutputConnectorEClass, CloneMediatorTargetOutputConnector.class, "CloneMediatorTargetOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCloneMediatorTargetOutputConnector_SoapAction(), ecorePackage.getEString(), "soapAction", "soapAction", 0, 1, CloneMediatorTargetOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCloneMediatorTargetOutputConnector_ToAddress(), ecorePackage.getEString(), "toAddress", "toAddress", 0, 1, CloneMediatorTargetOutputConnector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cloneMediatorContainerEClass, CloneMediatorContainer.class, "CloneMediatorContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getCloneMediatorContainer_CloneTargetContainer(), this.getCloneTargetContainer(), null, "cloneTargetContainer", null, 0, -1, CloneMediatorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cloneTargetContainerEClass, CloneTargetContainer.class, "CloneTargetContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getCloneTargetContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, CloneTargetContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(iterateMediatorEClass, IterateMediator.class, "IterateMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getIterateMediator_IterateID(), ecorePackage.getEString(), "iterateID", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getIterateMediator_SequentialMediation(), ecorePackage.getEBoolean(), "sequentialMediation", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getIterateMediator_ContinueParent(), ecorePackage.getEBoolean(), "continueParent", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getIterateMediator_PreservePayload(), ecorePackage.getEBoolean(), "preservePayload", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_IterateExpression(), this.getNamespacedProperty(), null, "iterateExpression", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_AttachPath(), this.getNamespacedProperty(), null, "attachPath", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_Target(), this.getIterateTarget(), null, "target", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_InputConnector(), this.getIterateMediatorInputConnector(), null, "inputConnector", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_OutputConnector(), this.getIterateMediatorOutputConnector(), null, "outputConnector", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_TargetOutputConnector(), this.getIterateMediatorTargetOutputConnector(), null, "targetOutputConnector", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getIterateMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, IterateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(iterateMediatorInputConnectorEClass, IterateMediatorInputConnector.class, "IterateMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(iterateMediatorOutputConnectorEClass, IterateMediatorOutputConnector.class, "IterateMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(iterateMediatorTargetOutputConnectorEClass, IterateMediatorTargetOutputConnector.class, "IterateMediatorTargetOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(iterateTargetEClass, IterateTarget.class, "IterateTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getIterateTarget_SoapAction(), ecorePackage.getEString(), "soapAction", null, 0, 1, IterateTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getIterateTarget_ToAddress(), ecorePackage.getEString(), "toAddress", null, 0, 1, IterateTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractCommonTargetEClass, AbstractCommonTarget.class, "AbstractCommonTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractCommonTarget_SequenceType(), this.getTargetSequenceType(), "sequenceType", "NONE", 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractCommonTarget_Sequence(), this.getMediatorSequence(), null, "sequence", null, 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractCommonTarget_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractCommonTarget_EndpointType(), this.getTargetEndpointType(), "endpointType", "NONE", 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractCommonTarget_Endpoint(), this.getEndPoint(), null, "endpoint", null, 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractCommonTarget_EndpointKey(), this.getRegistryKeyProperty(), null, "endpointKey", null, 0, 1, AbstractCommonTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(mediatorSequenceEClass, MediatorSequence.class, "MediatorSequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getMediatorSequence_Anonymous(), ecorePackage.getEBoolean(), "anonymous", "false", 0, 1, MediatorSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getMediatorSequence_SequenceName(), ecorePackage.getEString(), "sequenceName", "sequence_name", 0, 1, MediatorSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMediatorSequence_Mediators(), this.getMediator(), null, "mediators", null, 0, -1, MediatorSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMediatorSequence_OnError(), this.getRegistryKeyProperty(), null, "onError", null, 0, 1, MediatorSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getMediatorSequence_Description(), ecorePackage.getEString(), "description", null, 0, 1, MediatorSequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cacheMediatorEClass, CacheMediator.class, "CacheMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCacheMediator_CacheId(), ecorePackage.getEString(), "cacheId", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_CacheScope(), this.getCacheScope(), "cacheScope", "PER_MEDIATOR", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_CacheAction(), this.getCacheAction(), "cacheAction", "FINDER", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_HashGenerator(), ecorePackage.getEString(), "hashGenerator", "org.wso2.caching.digest.DOMHashGenerator", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_CacheTimeout(), ecorePackage.getEInt(), "cacheTimeout", "120", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_MaxMessageSize(), ecorePackage.getEInt(), "maxMessageSize", "2000", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_ImplementationType(), this.getCacheImplementationType(), "implementationType", "IN_MEMORY", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_MaxEntryCount(), ecorePackage.getEInt(), "maxEntryCount", "1000", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCacheMediator_SequenceType(), this.getCacheSequenceType(), "sequenceType", "REGISTRY_REFERENCE", 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCacheMediator_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCacheMediator_InputConnector(), this.getCacheMediatorInputConnector(), null, "inputConnector", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCacheMediator_OutputConnector(), this.getCacheMediatorOutputConnector(), null, "outputConnector", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCacheMediator_OnHitOutputConnector(), this.getCacheMediatorOnHitOutputConnector(), null, "onHitOutputConnector", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCacheMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, CacheMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(cacheMediatorInputConnectorEClass, CacheMediatorInputConnector.class, "CacheMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(cacheMediatorOutputConnectorEClass, CacheMediatorOutputConnector.class, "CacheMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(cacheMediatorOnHitOutputConnectorEClass, CacheMediatorOnHitOutputConnector.class, "CacheMediatorOnHitOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(cacheOnHitBranchEClass, CacheOnHitBranch.class, "CacheOnHitBranch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xQueryMediatorEClass, XQueryMediator.class, "XQueryMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getXQueryMediator_Variables(), this.getXQueryVariable(), null, "variables", null, 0, -1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_TargetXPath(), this.getNamespacedProperty(), null, "targetXPath", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getXQueryMediator_ScriptKeyType(), this.getKeyType(), "scriptKeyType", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_StaticScriptKey(), this.getRegistryKeyProperty(), null, "staticScriptKey", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_DynamicScriptKey(), this.getNamespacedProperty(), null, "dynamicScriptKey", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_QueryKey(), this.getRegistryKeyProperty(), null, "queryKey", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_InputConnector(), this.getXQueryMediatorInputConnector(), null, "inputConnector", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryMediator_OutputConnector(), this.getXQueryMediatorOutputConnector(), null, "outputConnector", null, 0, 1, XQueryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(xQueryMediatorInputConnectorEClass, XQueryMediatorInputConnector.class, "XQueryMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xQueryMediatorOutputConnectorEClass, XQueryMediatorOutputConnector.class, "XQueryMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(xQueryVariableEClass, XQueryVariable.class, "XQueryVariable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getXQueryVariable_VariableName(), ecorePackage.getEString(), "variableName", "variable_name", 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getXQueryVariable_VariableType(), this.getXQueryVariableType(), "variableType", "STRING", 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getXQueryVariable_ValueType(), this.getXQueryVariableValueType(), "valueType", "LITERAL", 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getXQueryVariable_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "literal_value", 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryVariable_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getXQueryVariable_ValueKey(), this.getRegistryKeyProperty(), null, "valueKey", null, 0, 1, XQueryVariable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(calloutMediatorEClass, CalloutMediator.class, "CalloutMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCalloutMediator_ServiceURL(), ecorePackage.getEString(), "serviceURL", "service_url", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_SoapAction(), ecorePackage.getEString(), "soapAction", "soap_action", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_PathToAxis2xml(), ecorePackage.getEString(), "pathToAxis2xml", "", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_PathToAxis2Repository(), ecorePackage.getEString(), "pathToAxis2Repository", "", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_PayloadType(), this.getCalloutPayloadType(), "payloadType", "MESSAGE_ELEMENT", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCalloutMediator_PayloadMessageXpath(), this.getNamespacedProperty(), null, "payloadMessageXpath", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCalloutMediator_PayloadRegistryKey(), this.getRegistryKeyProperty(), null, "payloadRegistryKey", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_ResultType(), this.getCalloutResultType(), "resultType", "MESSAGE_ELEMENT", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCalloutMediator_ResultMessageXpath(), this.getNamespacedProperty(), null, "resultMessageXpath", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_ResultContextProperty(), ecorePackage.getEString(), "resultContextProperty", "context_property_name", 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCalloutMediator_PassHeaders(), ecorePackage.getEBoolean(), "passHeaders", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCalloutMediator_InputConnector(), this.getCalloutMediatorInputConnector(), null, "inputConnector", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCalloutMediator_OutputConnector(), this.getCalloutMediatorOutputConnector(), null, "outputConnector", null, 0, 1, CalloutMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(calloutMediatorInputConnectorEClass, CalloutMediatorInputConnector.class, "CalloutMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(calloutMediatorOutputConnectorEClass, CalloutMediatorOutputConnector.class, "CalloutMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(rmSequenceMediatorEClass, RMSequenceMediator.class, "RMSequenceMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRMSequenceMediator_RmSpecVersion(), this.getRMSpecVersion(), "rmSpecVersion", "VERSION_1_0", 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRMSequenceMediator_SequenceType(), this.getRMSequenceType(), "sequenceType", "SINGLE_MESSAGE", 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRMSequenceMediator_CorrelationXpath(), this.getNamespacedProperty(), null, "correlationXpath", null, 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRMSequenceMediator_LastMessageXpath(), this.getNamespacedProperty(), null, "lastMessageXpath", null, 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRMSequenceMediator_InputConnector(), this.getRMSequenceMediatorInputConnector(), null, "inputConnector", null, 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRMSequenceMediator_OutputConnector(), this.getRMSequenceMediatorOutputConnector(), null, "outputConnector", null, 0, 1, RMSequenceMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(rmSequenceMediatorInputConnectorEClass, RMSequenceMediatorInputConnector.class, "RMSequenceMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(rmSequenceMediatorOutputConnectorEClass, RMSequenceMediatorOutputConnector.class, "RMSequenceMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(transactionMediatorEClass, TransactionMediator.class, "TransactionMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getTransactionMediator_Action(), this.getTransactionAction(), "action", null, 0, 1, TransactionMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTransactionMediator_InputConnector(), this.getTransactionMediatorInputConnector(), null, "inputConnector", null, 0, 1, TransactionMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTransactionMediator_OutputConnector(), this.getTransactionMediatorOutputConnector(), null, "outputConnector", null, 0, 1, TransactionMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(transactionMediatorInputConnectorEClass, TransactionMediatorInputConnector.class, "TransactionMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(transactionMediatorOutputConnectorEClass, TransactionMediatorOutputConnector.class, "TransactionMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(oAuthMediatorEClass, OAuthMediator.class, "OAuthMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getOAuthMediator_RemoteServiceUrl(), ecorePackage.getEString(), "remoteServiceUrl", "service_url", 0, 1, OAuthMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getOAuthMediator_InputConnector(), this.getOAuthMediatorInputConnector(), null, "inputConnector", null, 0, 1, OAuthMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getOAuthMediator_OutputConnector(), this.getOAuthMediatorOutputConnector(), null, "outputConnector", null, 0, 1, OAuthMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(oAuthMediatorInputConnectorEClass, OAuthMediatorInputConnector.class, "OAuthMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(oAuthMediatorOutputConnectorEClass, OAuthMediatorOutputConnector.class, "OAuthMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(autoscaleInMediatorEClass, AutoscaleInMediator.class, "AutoscaleInMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(autoscaleOutMediatorEClass, AutoscaleOutMediator.class, "AutoscaleOutMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(headerMediatorEClass, HeaderMediator.class, "HeaderMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getHeaderMediator_HeaderName(), this.getNamespacedProperty(), null, "headerName", null, 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getHeaderMediator_HeaderAction(), this.getHeaderAction(), "headerAction", "", 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getHeaderMediator_ValueType(), this.getHeaderValueType(), "valueType", "", 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getHeaderMediator_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "header_value", 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getHeaderMediator_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getHeaderMediator_InputConnector(), this.getHeaderMediatorInputConnector(), null, "inputConnector", null, 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getHeaderMediator_OutputConnector(), this.getHeaderMediatorOutputConnector(), null, "outputConnector", null, 0, 1, HeaderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(headerMediatorInputConnectorEClass, HeaderMediatorInputConnector.class, "HeaderMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(headerMediatorOutputConnectorEClass, HeaderMediatorOutputConnector.class, "HeaderMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(throttleMediatorEClass, ThrottleMediator.class, "ThrottleMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getThrottleMediator_GroupId(), ecorePackage.getEString(), "groupId", "group_id", 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottleMediator_PolicyType(), this.getThrottlePolicyType(), "policyType", "INLINE", 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_PolicyKey(), this.getRegistryKeyProperty(), null, "policyKey", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottleMediator_MaxConcurrentAccessCount(), ecorePackage.getEInt(), "maxConcurrentAccessCount", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_PolicyEntries(), this.getThrottlePolicyEntry(), null, "policyEntries", null, 0, -1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_PolicyConfiguration(), this.getThrottlePolicyConfiguration(), null, "policyConfiguration", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnAcceptBranch(), this.getThrottleOnAcceptBranch(), null, "onAcceptBranch", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnRejectBranch(), this.getThrottleOnRejectBranch(), null, "onRejectBranch", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_InputConnector(), this.getThrottleMediatorInputConnector(), null, "inputConnector", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OutputConnector(), this.getThrottleMediatorOutputConnector(), null, "outputConnector", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnAcceptOutputConnector(), this.getThrottleMediatorOnAcceptOutputConnector(), null, "onAcceptOutputConnector", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnRejectOutputConnector(), this.getThrottleMediatorOnRejectOutputConnector(), null, "onRejectOutputConnector", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_ThrottleContainer(), this.getThrottleContainer(), null, "throttleContainer", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottleMediator_OnAcceptBranchsequenceType(), this.getThrottleSequenceType(), "OnAcceptBranchsequenceType", "ANONYMOUS", 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnAcceptBranchsequenceKey(), this.getRegistryKeyProperty(), null, "OnAcceptBranchsequenceKey", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottleMediator_OnRejectBranchsequenceType(), this.getThrottleSequenceType(), "OnRejectBranchsequenceType", "ANONYMOUS", 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleMediator_OnRejectBranchsequenceKey(), this.getRegistryKeyProperty(), null, "OnRejectBranchsequenceKey", null, 0, 1, ThrottleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleMediatorInputConnectorEClass, ThrottleMediatorInputConnector.class, "ThrottleMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(throttleMediatorOutputConnectorEClass, ThrottleMediatorOutputConnector.class, "ThrottleMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(throttleMediatorOnAcceptOutputConnectorEClass, ThrottleMediatorOnAcceptOutputConnector.class, "ThrottleMediatorOnAcceptOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(throttleMediatorOnRejectOutputConnectorEClass, ThrottleMediatorOnRejectOutputConnector.class, "ThrottleMediatorOnRejectOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(throttlePolicyConfigurationEClass, ThrottlePolicyConfiguration.class, "ThrottlePolicyConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getThrottlePolicyConfiguration_PolicyType(), this.getThrottlePolicyType(), "policyType", "INLINE", 0, 1, ThrottlePolicyConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottlePolicyConfiguration_PolicyKey(), this.getRegistryKeyProperty(), null, "policyKey", null, 0, 1, ThrottlePolicyConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyConfiguration_MaxConcurrentAccessCount(), ecorePackage.getEInt(), "maxConcurrentAccessCount", null, 0, 1, ThrottlePolicyConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottlePolicyConfiguration_PolicyEntries(), this.getThrottlePolicyEntry(), null, "policyEntries", null, 0, -1, ThrottlePolicyConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttlePolicyEntryEClass, ThrottlePolicyEntry.class, "ThrottlePolicyEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getThrottlePolicyEntry_ThrottleType(), this.getThrottleConditionType(), "throttleType", "IP", 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyEntry_ThrottleRange(), ecorePackage.getEString(), "throttleRange", "other", 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyEntry_AccessType(), this.getThrottleAccessType(), "accessType", "ALLOW", 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyEntry_MaxRequestCount(), ecorePackage.getEInt(), "maxRequestCount", null, 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyEntry_UnitTime(), ecorePackage.getEInt(), "unitTime", null, 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getThrottlePolicyEntry_ProhibitPeriod(), ecorePackage.getEInt(), "prohibitPeriod", null, 0, 1, ThrottlePolicyEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleOnAcceptBranchEClass, ThrottleOnAcceptBranch.class, "ThrottleOnAcceptBranch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getThrottleOnAcceptBranch_SequenceType(), this.getThrottleSequenceType(), "sequenceType", "ANONYMOUS", 0, 1, ThrottleOnAcceptBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleOnAcceptBranch_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, ThrottleOnAcceptBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleOnRejectBranchEClass, ThrottleOnRejectBranch.class, "ThrottleOnRejectBranch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getThrottleOnRejectBranch_SequenceType(), this.getThrottleSequenceType(), "sequenceType", "ANONYMOUS", 0, 1, ThrottleOnRejectBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleOnRejectBranch_SequenceKey(), this.getRegistryKeyProperty(), null, "sequenceKey", null, 0, 1, ThrottleOnRejectBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleContainerEClass, ThrottleContainer.class, "ThrottleContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getThrottleContainer_OnAcceptContainer(), this.getThrottleOnAcceptContainer(), null, "onAcceptContainer", null, 0, 1, ThrottleContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getThrottleContainer_OnRejectContainer(), this.getThrottleOnRejectContainer(), null, "onRejectContainer", null, 0, 1, ThrottleContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleOnAcceptContainerEClass, ThrottleOnAcceptContainer.class, "ThrottleOnAcceptContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getThrottleOnAcceptContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ThrottleOnAcceptContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(throttleOnRejectContainerEClass, ThrottleOnRejectContainer.class, "ThrottleOnRejectContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getThrottleOnRejectContainer_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ThrottleOnRejectContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(commandMediatorEClass, CommandMediator.class, "CommandMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCommandMediator_ClassName(), ecorePackage.getEString(), "className", "class_name", 0, 1, CommandMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCommandMediator_Properties(), this.getCommandProperty(), null, "properties", null, 0, -1, CommandMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCommandMediator_InputConnector(), this.getCommandMediatorInputConnector(), null, "inputConnector", null, 0, 1, CommandMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCommandMediator_OutputConnector(), this.getCommandMediatorOutputConnector(), null, "outputConnector", null, 0, 1, CommandMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(commandMediatorInputConnectorEClass, CommandMediatorInputConnector.class, "CommandMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(commandMediatorOutputConnectorEClass, CommandMediatorOutputConnector.class, "CommandMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(commandPropertyEClass, CommandProperty.class, "CommandProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCommandProperty_PropertyName(), ecorePackage.getEString(), "propertyName", "property_name", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCommandProperty_ValueType(), this.getCommandPropertyValueType(), "valueType", "LITERAL", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCommandProperty_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "literal_value", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCommandProperty_ValueContextPropertyName(), ecorePackage.getEString(), "valueContextPropertyName", "context_property_name", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCommandProperty_ValueMessageElementXpath(), this.getNamespacedProperty(), null, "valueMessageElementXpath", null, 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCommandProperty_ContextAction(), this.getCommandPropertyContextAction(), "contextAction", "READ_AND_UPDATE_CONTEXT", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCommandProperty_MessageAction(), this.getCommandPropertyMessageAction(), "messageAction", "READ_AND_UPDATE_MESSAGE", 0, 1, CommandProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractSqlExecutorMediatorEClass, AbstractSqlExecutorMediator.class, "AbstractSqlExecutorMediator", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionType(), this.getSqlExecutorConnectionType(), "connectionType", "DB_CONNECTION", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionDsType(), this.getSqlExecutorDatasourceType(), "connectionDsType", "EXTERNAL", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionDbDriver(), ecorePackage.getEString(), "connectionDbDriver", "driver_class", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionDsInitialContext(), ecorePackage.getEString(), "connectionDsInitialContext", "initial_context", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionDsName(), ecorePackage.getEString(), "connectionDsName", "datasource_name", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionURL(), ecorePackage.getEString(), "connectionURL", "connection_url", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionUsername(), ecorePackage.getEString(), "connectionUsername", "username", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_ConnectionPassword(), ecorePackage.getEString(), "connectionPassword", "password", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyAutocommit(), this.getSqlExecutorBooleanValue(), "propertyAutocommit", "DEFAULT", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyIsolation(), this.getSqlExecutorIsolationLevel(), "propertyIsolation", "DEFAULT", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyMaxactive(), ecorePackage.getEInt(), "propertyMaxactive", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyMaxidle(), ecorePackage.getEInt(), "propertyMaxidle", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyMaxopenstatements(), ecorePackage.getEInt(), "propertyMaxopenstatements", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyMaxwait(), ecorePackage.getEInt(), "propertyMaxwait", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyMinidle(), ecorePackage.getEInt(), "propertyMinidle", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyPoolstatements(), this.getSqlExecutorBooleanValue(), "propertyPoolstatements", "DEFAULT", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyTestonborrow(), this.getSqlExecutorBooleanValue(), "propertyTestonborrow", "DEFAULT", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyTestwhileidle(), this.getSqlExecutorBooleanValue(), "propertyTestwhileidle", "DEFAULT", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyValidationquery(), ecorePackage.getEString(), "propertyValidationquery", "", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractSqlExecutorMediator_PropertyInitialsize(), ecorePackage.getEInt(), "propertyInitialsize", "-1", 0, 1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractSqlExecutorMediator_SqlStatements(), this.getSqlStatement(), null, "sqlStatements", null, 0, -1, AbstractSqlExecutorMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sqlStatementEClass, SqlStatement.class, "SqlStatement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSqlStatement_QueryString(), ecorePackage.getEString(), "queryString", "sql_query", 0, 1, SqlStatement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSqlStatement_Parameters(), this.getSqlParameterDefinition(), null, "parameters", null, 0, -1, SqlStatement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSqlStatement_ResultsEnabled(), ecorePackage.getEBoolean(), "resultsEnabled", "false", 0, 1, SqlStatement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSqlStatement_Results(), this.getSqlResultMapping(), null, "results", null, 0, -1, SqlStatement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sqlParameterDefinitionEClass, SqlParameterDefinition.class, "SqlParameterDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSqlParameterDefinition_DataType(), this.getSqlParameterDataType(), "dataType", "CHAR", 0, 1, SqlParameterDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSqlParameterDefinition_ValueType(), this.getSqlParameterValueType(), "valueType", "LITERAL", 0, 1, SqlParameterDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSqlParameterDefinition_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "value", 0, 1, SqlParameterDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSqlParameterDefinition_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, SqlParameterDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sqlResultMappingEClass, SqlResultMapping.class, "SqlResultMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSqlResultMapping_PropertyName(), ecorePackage.getEString(), "propertyName", "message_context_property_name", 0, 1, SqlResultMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSqlResultMapping_ColumnId(), ecorePackage.getEString(), "columnId", "column_name_or_index", 0, 1, SqlResultMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(dbLookupMediatorEClass, DBLookupMediator.class, "DBLookupMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getDBLookupMediator_InputConnector(), this.getDBLookupMediatorInputConnector(), null, "inputConnector", null, 0, 1, DBLookupMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getDBLookupMediator_OutputConnector(), this.getDBLookupMediatorOutputConnector(), null, "outputConnector", null, 0, 1, DBLookupMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(dbLookupMediatorInputConnectorEClass, DBLookupMediatorInputConnector.class, "DBLookupMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(dbLookupMediatorOutputConnectorEClass, DBLookupMediatorOutputConnector.class, "DBLookupMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(dbReportMediatorEClass, DBReportMediator.class, "DBReportMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getDBReportMediator_ConnectionUseTransaction(), ecorePackage.getEBoolean(), "connectionUseTransaction", "false", 0, 1, DBReportMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getDBReportMediator_InputConnector(), this.getDBReportMediatorInputConnector(), null, "inputConnector", null, 0, 1, DBReportMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getDBReportMediator_OutputConnector(), this.getDBReportMediatorOutputConnector(), null, "outputConnector", null, 0, 1, DBReportMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(dbReportMediatorInputConnectorEClass, DBReportMediatorInputConnector.class, "DBReportMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(dbReportMediatorOutputConnectorEClass, DBReportMediatorOutputConnector.class, "DBReportMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleMediatorEClass, RuleMediator.class, "RuleMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRuleMediator_RuleSetURI(), ecorePackage.getEString(), "RuleSetURI", "", 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_RuleSetSourceType(), this.getRuleSourceType(), "ruleSetSourceType", "INLINE", 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_RuleSetSourceCode(), ecorePackage.getEString(), "ruleSetSourceCode", "<code/>", 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_RuleSetSourceKey(), this.getRegistryKeyProperty(), null, "ruleSetSourceKey", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_RuleSetProperties(), this.getRuleSetCreationProperty(), null, "ruleSetProperties", null, 0, -1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_StatefulSession(), ecorePackage.getEBoolean(), "statefulSession", "true", 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_RuleSessionProperties(), this.getRuleSessionProperty(), null, "RuleSessionProperties", null, 0, -1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_FactsConfiguration(), this.getRuleFactsConfiguration(), null, "factsConfiguration", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_ResultsConfiguration(), this.getRuleResultsConfiguration(), null, "resultsConfiguration", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_ChildMediatorsConfiguration(), this.getRuleChildMediatorsConfiguration(), null, "childMediatorsConfiguration", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_InputConnector(), this.getRuleMediatorInputConnector(), null, "inputConnector", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_OutputConnector(), this.getRuleMediatorOutputConnector(), null, "outputConnector", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_ChildMediatorsOutputConnector(), this.getRuleMediatorChildMediatorsOutputConnector(), null, "childMediatorsOutputConnector", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_SourceValue(), ecorePackage.getEString(), "sourceValue", "", 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_SourceXpath(), this.getNamespacedProperty(), null, "sourceXpath", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_TargetValue(), ecorePackage.getEString(), "targetValue", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_TargetResultXpath(), this.getNamespacedProperty(), null, "targetResultXpath", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleMediator_TargetXpath(), this.getNamespacedProperty(), null, "targetXpath", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_TargetAction(), this.getRuleActions(), "targetAction", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_InputWrapperName(), ecorePackage.getEString(), "InputWrapperName", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_InputNameSpace(), ecorePackage.getEString(), "InputNameSpace", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_OutputWrapperName(), ecorePackage.getEString(), "OutputWrapperName", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleMediator_OutputNameSpace(), ecorePackage.getEString(), "OutputNameSpace", null, 0, 1, RuleMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleMediatorInputConnectorEClass, RuleMediatorInputConnector.class, "RuleMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleMediatorOutputConnectorEClass, RuleMediatorOutputConnector.class, "RuleMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleMediatorChildMediatorsOutputConnectorEClass, RuleMediatorChildMediatorsOutputConnector.class, "RuleMediatorChildMediatorsOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleSetCreationPropertyEClass, RuleSetCreationProperty.class, "RuleSetCreationProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleSessionPropertyEClass, RuleSessionProperty.class, "RuleSessionProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(ruleFactsConfigurationEClass, RuleFactsConfiguration.class, "RuleFactsConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRuleFactsConfiguration_Facts(), this.getRuleFact(), null, "facts", null, 0, -1, RuleFactsConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleFactEClass, RuleFact.class, "RuleFact", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRuleFact_FactType(), this.getRuleFactType(), "factType", "CUSTOM", 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleFact_FactCustomType(), ecorePackage.getEString(), "factCustomType", "custom_type", 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleFact_FactName(), ecorePackage.getEString(), "factName", "fact_name", 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleFact_ValueType(), this.getRuleFactValueType(), "valueType", "LITERAL", 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleFact_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "value", 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleFact_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleFact_ValueKey(), this.getRegistryKeyProperty(), null, "valueKey", null, 0, 1, RuleFact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleResultsConfigurationEClass, RuleResultsConfiguration.class, "RuleResultsConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRuleResultsConfiguration_Results(), this.getRuleResult(), null, "results", null, 0, -1, RuleResultsConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleResultEClass, RuleResult.class, "RuleResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRuleResult_ResultType(), this.getRuleResultType(), "resultType", "CUSTOM", 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleResult_ResultCustomType(), ecorePackage.getEString(), "resultCustomType", "custom_type", 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleResult_ResultName(), ecorePackage.getEString(), "resultName", "result_name", 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleResult_ValueType(), this.getRuleResultValueType(), "valueType", "LITERAL", 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getRuleResult_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "value", 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleResult_ValueExpression(), this.getNamespacedProperty(), null, "valueExpression", null, 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRuleResult_ValueKey(), this.getRegistryKeyProperty(), null, "valueKey", null, 0, 1, RuleResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleChildMediatorsConfigurationEClass, RuleChildMediatorsConfiguration.class, "RuleChildMediatorsConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(callTemplateParameterEClass, CallTemplateParameter.class, "CallTemplateParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCallTemplateParameter_ParameterName(), ecorePackage.getEString(), "parameterName", null, 0, 1, CallTemplateParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCallTemplateParameter_TemplateParameterType(), this.getRuleOptionType(), "templateParameterType", null, 0, 1, CallTemplateParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCallTemplateParameter_ParameterValue(), ecorePackage.getEString(), "parameterValue", null, 0, 1, CallTemplateParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCallTemplateParameter_ParameterExpression(), this.getNamespacedProperty(), null, "parameterExpression", null, 0, 1, CallTemplateParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(callTemplateMediatorEClass, CallTemplateMediator.class, "CallTemplateMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCallTemplateMediator_TargetTemplate(), ecorePackage.getEString(), "targetTemplate", null, 0, 1, CallTemplateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCallTemplateMediator_TemplateParameters(), this.getCallTemplateParameter(), null, "templateParameters", null, 0, -1, CallTemplateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCallTemplateMediator_InputConnector(), this.getCallTemplateMediatorInputConnector(), null, "inputConnector", null, 0, 1, CallTemplateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getCallTemplateMediator_OutputConnector(), this.getCallTemplateMediatorOutputConnector(), null, "outputConnector", null, 0, 1, CallTemplateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(callTemplateMediatorInputConnectorEClass, CallTemplateMediatorInputConnector.class, "CallTemplateMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(callTemplateMediatorOutputConnectorEClass, CallTemplateMediatorOutputConnector.class, "CallTemplateMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(smooksMediatorEClass, SmooksMediator.class, "SmooksMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSmooksMediator_ConfigurationKey(), this.getRegistryKeyProperty(), null, "configurationKey", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSmooksMediator_InputType(), this.getSmooksIODataType(), "inputType", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSmooksMediator_InputExpression(), this.getNamespacedProperty(), null, "inputExpression", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSmooksMediator_OutputType(), this.getSmooksIODataType(), "outputType", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSmooksMediator_OutputExpression(), this.getNamespacedProperty(), null, "outputExpression", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSmooksMediator_OutputProperty(), ecorePackage.getEString(), "outputProperty", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSmooksMediator_OutputAction(), this.getExpressionAction(), "outputAction", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSmooksMediator_OutputMethod(), this.getOutputMethod(), "outputMethod", "", 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSmooksMediator_InputConnector(), this.getSmooksMediatorInputConnector(), null, "inputConnector", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSmooksMediator_OutputConnector(), this.getSmooksMediatorOutputConnector(), null, "outputConnector", null, 0, 1, SmooksMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(smooksMediatorInputConnectorEClass, SmooksMediatorInputConnector.class, "SmooksMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(smooksMediatorOutputConnectorEClass, SmooksMediatorOutputConnector.class, "SmooksMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(storeMediatorEClass, StoreMediator.class, "StoreMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getStoreMediator_MessageStore(), ecorePackage.getEString(), "messageStore", null, 0, 1, StoreMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getStoreMediator_OnStoreSequence(), this.getRegistryKeyProperty(), null, "onStoreSequence", null, 0, 1, StoreMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getStoreMediator_InputConnector(), this.getStoreMediatorInputConnector(), null, "inputConnector", null, 0, 1, StoreMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getStoreMediator_OutputConnector(), this.getStoreMediatorOutputConnector(), null, "outputConnector", null, 0, 1, StoreMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(storeMediatorInputConnectorEClass, StoreMediatorInputConnector.class, "StoreMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(storeMediatorOutputConnectorEClass, StoreMediatorOutputConnector.class, "StoreMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(builderMediatorEClass, BuilderMediator.class, "BuilderMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getBuilderMediator_MessageBuilders(), this.getMessageBuilder(), null, "messageBuilders", null, 0, -1, BuilderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getBuilderMediator_InputConnector(), this.getBuilderMediatorInputConnector(), null, "inputConnector", null, 0, 1, BuilderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getBuilderMediator_OutputConnector(), this.getBuilderMediatorOutputConector(), null, "outputConnector", null, 0, 1, BuilderMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(builderMediatorInputConnectorEClass, BuilderMediatorInputConnector.class, "BuilderMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(builderMediatorOutputConectorEClass, BuilderMediatorOutputConector.class, "BuilderMediatorOutputConector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(messageBuilderEClass, MessageBuilder.class, "MessageBuilder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getMessageBuilder_ContentType(), ecorePackage.getEString(), "contentType", null, 0, 1, MessageBuilder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getMessageBuilder_BuilderClass(), ecorePackage.getEString(), "builderClass", null, 0, 1, MessageBuilder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getMessageBuilder_FormatterClass(), ecorePackage.getEString(), "formatterClass", null, 0, 1, MessageBuilder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(payloadFactoryMediatorEClass, PayloadFactoryMediator.class, "PayloadFactoryMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getPayloadFactoryMediator_Format(), ecorePackage.getEString(), "format", null, 0, 1, PayloadFactoryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPayloadFactoryMediator_Args(), this.getPayloadFactoryArgument(), null, "args", null, 0, -1, PayloadFactoryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPayloadFactoryMediator_InputConnector(), this.getPayloadFactoryMediatorInputConnector(), null, "inputConnector", null, 0, 1, PayloadFactoryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPayloadFactoryMediator_OutputConnector(), this.getPayloadFactoryMediatorOutputConnector(), null, "outputConnector", null, 0, 1, PayloadFactoryMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(payloadFactoryMediatorInputConnectorEClass, PayloadFactoryMediatorInputConnector.class, "PayloadFactoryMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(payloadFactoryMediatorOutputConnectorEClass, PayloadFactoryMediatorOutputConnector.class, "PayloadFactoryMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(payloadFactoryArgumentEClass, PayloadFactoryArgument.class, "PayloadFactoryArgument", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getPayloadFactoryArgument_ArgumentType(), this.getPayloadFactoryArgumentType(), "argumentType", null, 0, 1, PayloadFactoryArgument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPayloadFactoryArgument_ArgumentValue(), ecorePackage.getEString(), "argumentValue", null, 0, 1, PayloadFactoryArgument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPayloadFactoryArgument_ArgumentExpression(), this.getNamespacedProperty(), null, "argumentExpression", null, 0, 1, PayloadFactoryArgument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(conditionalRouteBranchEClass, ConditionalRouteBranch.class, "ConditionalRouteBranch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getConditionalRouteBranch_BreakAfterRoute(), ecorePackage.getEBoolean(), "breakAfterRoute", null, 0, 1, ConditionalRouteBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouteBranch_EvaluatorExpression(), this.getEvaluatorExpressionProperty(), null, "evaluatorExpression", null, 0, 1, ConditionalRouteBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouteBranch_TargetSequence(), this.getRegistryKeyProperty(), null, "targetSequence", null, 0, 1, ConditionalRouteBranch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(conditionalRouterMediatorEClass, ConditionalRouterMediator.class, "ConditionalRouterMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getConditionalRouterMediator_ContinueRoute(), ecorePackage.getEBoolean(), "continueRoute", null, 0, 1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouterMediator_ConditionalRouteBranches(), this.getConditionalRouteBranch(), null, "conditionalRouteBranches", null, 0, -1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouterMediator_InputConnector(), this.getConditionalRouterMediatorInputConnector(), null, "inputConnector", null, 0, 1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouterMediator_OutputConnector(), this.getConditionalRouterMediatorOutputConnector(), null, "outputConnector", null, 0, 1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouterMediator_AdditionalOutputConnector(), this.getConditionalRouterMediatorAdditionalOutputConnector(), null, "additionalOutputConnector", null, 0, 1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConditionalRouterMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ConditionalRouterMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(conditionalRouterMediatorInputConnectorEClass, ConditionalRouterMediatorInputConnector.class, "ConditionalRouterMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(conditionalRouterMediatorOutputConnectorEClass, ConditionalRouterMediatorOutputConnector.class, "ConditionalRouterMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(conditionalRouterMediatorAdditionalOutputConnectorEClass, ConditionalRouterMediatorAdditionalOutputConnector.class, "ConditionalRouterMediatorAdditionalOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sendMediatorEClass, SendMediator.class, "SendMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSendMediator_EndPoint(), this.getEndPoint(), null, "EndPoint", null, 0, -1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_InputConnector(), this.getSendMediatorInputConnector(), null, "InputConnector", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_OutputConnector(), this.getSendMediatorOutputConnector(), null, "OutputConnector", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSendMediator_ReceivingSequenceType(), this.getReceivingSequenceType(), "receivingSequenceType", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_StaticReceivingSequence(), this.getRegistryKeyProperty(), null, "StaticReceivingSequence", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_DynamicReceivingSequence(), this.getNamespacedProperty(), null, "DynamicReceivingSequence", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_EndpointOutputConnector(), this.getSendMediatorEndpointOutputConnector(), null, "endpointOutputConnector", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSendMediator_EndpointFlow(), this.getEndpointFlow(), null, "endpointFlow", null, 0, 1, SendMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sendContainerEClass, SendContainer.class, "SendContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSendContainer_EndpointFlow(), this.getEndpointFlow(), null, "endpointFlow", null, 0, 1, SendContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sendMediatorInputConnectorEClass, SendMediatorInputConnector.class, "SendMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sendMediatorOutputConnectorEClass, SendMediatorOutputConnector.class, "SendMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sendMediatorEndpointOutputConnectorEClass, SendMediatorEndpointOutputConnector.class, "SendMediatorEndpointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(failoverEndPointEClass, FailoverEndPoint.class, "FailoverEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFailoverEndPoint_InputConnector(), this.getFailoverEndPointInputConnector(), null, "inputConnector", null, 0, 1, FailoverEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFailoverEndPoint_OutputConnector(), this.getFailoverEndPointOutputConnector(), null, "OutputConnector", null, 0, -1, FailoverEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFailoverEndPoint_WestOutputConnector(), this.getFailoverEndPointWestOutputConnector(), null, "westOutputConnector", null, 0, 1, FailoverEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(failoverEndPointInputConnectorEClass, FailoverEndPointInputConnector.class, "FailoverEndPointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(failoverEndPointOutputConnectorEClass, FailoverEndPointOutputConnector.class, "FailoverEndPointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(failoverEndPointWestOutputConnectorEClass, FailoverEndPointWestOutputConnector.class, "FailoverEndPointWestOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(parentEndPointEClass, ParentEndPoint.class, "ParentEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getParentEndPoint_Children(), this.getEndPoint(), null, "Children", null, 0, -1, ParentEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(wsdlEndPointEClass, WSDLEndPoint.class, "WSDLEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getWSDLEndPoint_WSDLDefinition(), this.getWSDLDefinition(), null, "WSDLDefinition", null, 0, -1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getWSDLEndPoint_WSDLDescription(), this.getWSDLDescription(), null, "WSDLDescription", null, 0, -1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getWSDLEndPoint_InputConnector(), this.getWSDLEndPointInputConnector(), null, "inputConnector", null, 0, 1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getWSDLEndPoint_OutputConnector(), this.getWSDLEndPointOutputConnector(), null, "outputConnector", null, 0, 1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getWSDLEndPoint_WsdlUri(), ecorePackage.getEString(), "wsdlUri", null, 0, 1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getWSDLEndPoint_Service(), ecorePackage.getEString(), "service", null, 0, 1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getWSDLEndPoint_Port(), ecorePackage.getEString(), "port", null, 0, 1, WSDLEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(wsdlDefinitionEClass, WSDLDefinition.class, "WSDLDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(wsdlDescriptionEClass, WSDLDescription.class, "WSDLDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(wsdlEndPointInputConnectorEClass, WSDLEndPointInputConnector.class, "WSDLEndPointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(wsdlEndPointOutputConnectorEClass, WSDLEndPointOutputConnector.class, "WSDLEndPointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(loadBalanceEndPointEClass, LoadBalanceEndPoint.class, "LoadBalanceEndPoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getLoadBalanceEndPoint_Session(), this.getSession(), null, "session", null, 0, 1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLoadBalanceEndPoint_Failover(), ecorePackage.getEBoolean(), "failover", null, 0, 1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLoadBalanceEndPoint_Policy(), ecorePackage.getEString(), "policy", null, 0, 1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLoadBalanceEndPoint_InputConnector(), this.getLoadBalanceEndPointInputConnector(), null, "inputConnector", null, 0, 1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLoadBalanceEndPoint_OutputConnector(), this.getLoadBalanceEndPointOutputConnector(), null, "outputConnector", null, 0, -1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getLoadBalanceEndPoint_WestOutputConnector(), this.getLoadBalanceEndPointWestOutputConnector(), null, "westOutputConnector", null, 0, 1, LoadBalanceEndPoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(loadBalanceEndPointInputConnectorEClass, LoadBalanceEndPointInputConnector.class, "LoadBalanceEndPointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(loadBalanceEndPointOutputConnectorEClass, LoadBalanceEndPointOutputConnector.class, "LoadBalanceEndPointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(loadBalanceEndPointWestOutputConnectorEClass, LoadBalanceEndPointWestOutputConnector.class, "LoadBalanceEndPointWestOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(localEntryEClass, LocalEntry.class, "LocalEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getLocalEntry_EntryName(), ecorePackage.getEString(), "entryName", "entry_name", 0, 1, LocalEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLocalEntry_ValueType(), this.getLocalEntryValueType(), "valueType", "LITERAL", 0, 1, LocalEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLocalEntry_ValueLiteral(), ecorePackage.getEString(), "valueLiteral", "entry_value", 0, 1, LocalEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLocalEntry_ValueXML(), ecorePackage.getEString(), "valueXML", "<value/>", 0, 1, LocalEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLocalEntry_ValueURL(), ecorePackage.getEString(), "valueURL", "file:/path/to/resource.ext", 0, 1, LocalEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sessionEClass, Session.class, "Session", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSession_Type(), this.getType(), "type", null, 0, 1, Session.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sequencesEClass, Sequences.class, "Sequences", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSequences_OutputConnector(), this.getSequencesOutputConnector(), null, "outputConnector", null, 0, 1, Sequences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSequences_InputConnector(), this.getSequencesInputConnector(), null, "inputConnector", null, 0, 1, Sequences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSequences_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, Sequences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSequences_Name(), ecorePackage.getEString(), "name", null, 0, 1, Sequences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sequencesOutputConnectorEClass, SequencesOutputConnector.class, "SequencesOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(sequencesInputConnectorEClass, SequencesInputConnector.class, "SequencesInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(urlRewriteRuleActionEClass, URLRewriteRuleAction.class, "URLRewriteRuleAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getURLRewriteRuleAction_RuleAction(), this.getRuleActionType(), "ruleAction", "", 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteRuleAction_RuleFragment(), this.getRuleFragmentType(), "ruleFragment", "", 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteRuleAction_RuleOption(), this.getRuleOptionType(), "ruleOption", "", 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getURLRewriteRuleAction_ActionExpression(), this.getNamespacedProperty(), null, "actionExpression", null, 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteRuleAction_ActionValue(), ecorePackage.getEString(), "actionValue", "", 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteRuleAction_ActionRegex(), ecorePackage.getEString(), "actionRegex", "", 0, 1, URLRewriteRuleAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(urlRewriteRuleEClass, URLRewriteRule.class, "URLRewriteRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getURLRewriteRule_UrlRewriteRuleCondition(), this.getEvaluatorExpressionProperty(), null, "urlRewriteRuleCondition", null, 0, 1, URLRewriteRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getURLRewriteRule_RewriteRuleAction(), this.getURLRewriteRuleAction(), null, "rewriteRuleAction", null, 0, -1, URLRewriteRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(urlRewriteMediatorEClass, URLRewriteMediator.class, "URLRewriteMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getURLRewriteMediator_UrlRewriteRules(), this.getURLRewriteRule(), null, "urlRewriteRules", null, 0, -1, URLRewriteMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteMediator_InProperty(), ecorePackage.getEString(), "InProperty", null, 0, 1, URLRewriteMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getURLRewriteMediator_OutProperty(), ecorePackage.getEString(), "outProperty", null, 0, 1, URLRewriteMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getURLRewriteMediator_InputConnector(), this.getURLRewriteMediatorInputConnector(), null, "inputConnector", null, 0, 1, URLRewriteMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getURLRewriteMediator_OutputConnector(), this.getURLRewriteMediatorOutputConnector(), null, "outputConnector", null, 0, 1, URLRewriteMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(urlRewriteMediatorInputConnectorEClass, URLRewriteMediatorInputConnector.class, "URLRewriteMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(urlRewriteMediatorOutputConnectorEClass, URLRewriteMediatorOutputConnector.class, "URLRewriteMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(evaluatorExpressionPropertyEClass, EvaluatorExpressionProperty.class, "EvaluatorExpressionProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEvaluatorExpressionProperty_PrettyName(), ecorePackage.getEString(), "prettyName", null, 0, 1, EvaluatorExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEvaluatorExpressionProperty_EvaluatorName(), ecorePackage.getEString(), "evaluatorName", null, 0, 1, EvaluatorExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEvaluatorExpressionProperty_EvaluatorValue(), ecorePackage.getEString(), "evaluatorValue", null, 0, 1, EvaluatorExpressionProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(validateMediatorEClass, ValidateMediator.class, "ValidateMediator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getValidateMediator_SourceXpath(), this.getNamespacedProperty(), null, "sourceXpath", null, 0, 1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_Features(), this.getValidateFeature(), null, "features", null, 0, -1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_Schemas(), this.getValidateSchema(), null, "schemas", null, 0, -1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_InputConnector(), this.getValidateMediatorInputConnector(), null, "inputConnector", null, 0, 1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_OutputConnector(), this.getValidateMediatorOutputConnector(), null, "outputConnector", null, 0, 1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_OnFailOutputConnector(), this.getValidateMediatorOnFailOutputConnector(), null, "onFailOutputConnector", null, 0, 1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateMediator_MediatorFlow(), this.getMediatorFlow(), null, "mediatorFlow", null, 0, 1, ValidateMediator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(validateFeatureEClass, ValidateFeature.class, "ValidateFeature", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(validateSchemaEClass, ValidateSchema.class, "ValidateSchema", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getValidateSchema_ValidateStaticSchemaKey(), this.getRegistryKeyProperty(), null, "validateStaticSchemaKey", null, 0, 1, ValidateSchema.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateSchema_ValidateDynamicSchemaKey(), this.getNamespacedProperty(), null, "validateDynamicSchemaKey", null, 0, 1, ValidateSchema.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getValidateSchema_ValidateSchemaKeyType(), this.getKeyType(), "validateSchemaKeyType", null, 0, 1, ValidateSchema.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getValidateSchema_SchemaKey(), this.getRegistryKeyProperty(), null, "schemaKey", null, 0, 1, ValidateSchema.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(validateMediatorInputConnectorEClass, ValidateMediatorInputConnector.class, "ValidateMediatorInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(validateMediatorOutputConnectorEClass, ValidateMediatorOutputConnector.class, "ValidateMediatorOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(validateMediatorOnFailOutputConnectorEClass, ValidateMediatorOnFailOutputConnector.class, "ValidateMediatorOnFailOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(endpointDiagramEClass, EndpointDiagram.class, "EndpointDiagram", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getEndpointDiagram_Child(), this.getEndPoint(), null, "child", null, 0, 1, EndpointDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEndpointDiagram_Name(), ecorePackage.getEString(), "name", null, 0, 1, EndpointDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(namedEndpointEClass, NamedEndpoint.class, "NamedEndpoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getNamedEndpoint_InputConnector(), this.getNamedEndpointInputConnector(), null, "inputConnector", null, 0, 1, NamedEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getNamedEndpoint_OutputConnector(), this.getNamedEndpointOutputConnector(), null, "outputConnector", null, 0, 1, NamedEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getNamedEndpoint_Name(), ecorePackage.getEString(), "name", null, 0, 1, NamedEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(namedEndpointInputConnectorEClass, NamedEndpointInputConnector.class, "NamedEndpointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(namedEndpointOutputConnectorEClass, NamedEndpointOutputConnector.class, "NamedEndpointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(templateEClass, Template.class, "Template", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getTemplate_Name(), ecorePackage.getEString(), "name", null, 0, 1, Template.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTemplate_TemplateType(), this.getTemplateType(), "templateType", null, 0, 1, Template.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTemplate_Child(), this.getEsbElement(), null, "child", null, 0, 1, Template.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(taskEClass, Task.class, "Task", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getTask_TaskName(), ecorePackage.getEString(), "taskName", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTask_TaskGroup(), ecorePackage.getEString(), "taskGroup", "synapse.simple.quartz", 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTask_TriggerType(), this.getTaskTriggerType(), "triggerType", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTask_Count(), ecorePackage.getELong(), "count", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTask_Interval(), ecorePackage.getELong(), "interval", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTask_Cron(), ecorePackage.getEString(), "cron", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTask_PinnedServers(), ecorePackage.getEString(), "pinnedServers", null, 0, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTask_TaskImplementation(), ecorePackage.getEString(), "taskImplementation", "org.apache.synapse.startup.tasks.MessageInjector", 1, 1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTask_TaskProperties(), this.getTaskProperty(), null, "taskProperties", null, 0, -1, Task.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(nameValueTypePropertyEClass, NameValueTypeProperty.class, "NameValueTypeProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getNameValueTypeProperty_PropertyName(), ecorePackage.getEString(), "propertyName", null, 0, 1, NameValueTypeProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getNameValueTypeProperty_PropertyValue(), ecorePackage.getEString(), "propertyValue", null, 0, 1, NameValueTypeProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getNameValueTypeProperty_PropertyType(), this.getTaskPropertyType(), "propertyType", null, 0, 1, NameValueTypeProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(taskPropertyEClass, TaskProperty.class, "TaskProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(synapseAPIEClass, SynapseAPI.class, "SynapseAPI", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSynapseAPI_ApiName(), ecorePackage.getEString(), "apiName", "api_name", 1, 1, SynapseAPI.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSynapseAPI_Context(), ecorePackage.getEString(), "context", "/context", 1, 1, SynapseAPI.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSynapseAPI_HostName(), ecorePackage.getEString(), "hostName", null, 0, 1, SynapseAPI.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSynapseAPI_Port(), ecorePackage.getEInt(), "port", null, 0, 1, SynapseAPI.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSynapseAPI_Resources(), this.getAPIResource(), null, "resources", null, 0, -1, SynapseAPI.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(apiResourceEClass, APIResource.class, "APIResource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getAPIResource_InputConnector(), this.getAPIResourceInputConnector(), null, "inputConnector", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAPIResource_OutputConnector(), this.getAPIResourceOutputConnector(), null, "outputConnector", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAPIResource_FaultInputConnector(), this.getAPIResourceFaultInputConnector(), null, "faultInputConnector", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_UrlStyle(), this.getApiResourceUrlStyle(), "urlStyle", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_UriTemplate(), ecorePackage.getEString(), "uriTemplate", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_UrlMapping(), ecorePackage.getEString(), "urlMapping", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_AllowGet(), ecorePackage.getEBoolean(), "allowGet", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_AllowPost(), ecorePackage.getEBoolean(), "allowPost", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_AllowPut(), ecorePackage.getEBoolean(), "allowPut", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_AllowDelete(), ecorePackage.getEBoolean(), "allowDelete", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAPIResource_AllowOptions(), ecorePackage.getEBoolean(), "allowOptions", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAPIResource_Container(), this.getProxyServiceContainer(), null, "container", null, 0, 1, APIResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(apiResourceInputConnectorEClass, APIResourceInputConnector.class, "APIResourceInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(apiResourceOutputConnectorEClass, APIResourceOutputConnector.class, "APIResourceOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(apiResourceFaultInputConnectorEClass, APIResourceFaultInputConnector.class, "APIResourceFaultInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(apiResourceEndpointEClass, APIResourceEndpoint.class, "APIResourceEndpoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getAPIResourceEndpoint_InputConnector(), this.getAPIResourceEndpointInputConnector(), null, "inputConnector", null, 0, 1, APIResourceEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAPIResourceEndpoint_OutputConnector(), this.getAPIResourceEndpointOutputConnector(), null, "outputConnector", null, 0, 1, APIResourceEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(apiResourceEndpointInputConnectorEClass, APIResourceEndpointInputConnector.class, "APIResourceEndpointInputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(apiResourceEndpointOutputConnectorEClass, APIResourceEndpointOutputConnector.class, "APIResourceEndpointOutputConnector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		// Initialize enums and add enum literals
 		initEEnum(artifactTypeEEnum, ArtifactType.class, "ArtifactType");
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.SYNAPSE_CONFIG);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.PROXY);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.SEQUENCE);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.ENDPOINT);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.LOCAL_ENTRY);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.TASK);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.TEMPLATE);
 		addEEnumLiteral(artifactTypeEEnum, ArtifactType.API);
 
 		initEEnum(sequenceTypeEEnum, SequenceType.class, "SequenceType");
 		addEEnumLiteral(sequenceTypeEEnum, SequenceType.ANONYMOUS);
 		addEEnumLiteral(sequenceTypeEEnum, SequenceType.REGISTRY_REFERENCE);
 		addEEnumLiteral(sequenceTypeEEnum, SequenceType.NAMED_REFERENCE);
 
 		initEEnum(proxyWsdlTypeEEnum, ProxyWsdlType.class, "ProxyWsdlType");
 		addEEnumLiteral(proxyWsdlTypeEEnum, ProxyWsdlType.NONE);
 		addEEnumLiteral(proxyWsdlTypeEEnum, ProxyWsdlType.INLINE);
 		addEEnumLiteral(proxyWsdlTypeEEnum, ProxyWsdlType.SOURCE_URL);
 		addEEnumLiteral(proxyWsdlTypeEEnum, ProxyWsdlType.REGISTRY_KEY);
 
 		initEEnum(filterConditionTypeEEnum, FilterConditionType.class, "FilterConditionType");
 		addEEnumLiteral(filterConditionTypeEEnum, FilterConditionType.SOURCE_AND_REGEX);
 		addEEnumLiteral(filterConditionTypeEEnum, FilterConditionType.XPATH);
 
 		initEEnum(logCategoryEEnum, LogCategory.class, "LogCategory");
 		addEEnumLiteral(logCategoryEEnum, LogCategory.TRACE);
 		addEEnumLiteral(logCategoryEEnum, LogCategory.DEBUG);
 		addEEnumLiteral(logCategoryEEnum, LogCategory.INFO);
 		addEEnumLiteral(logCategoryEEnum, LogCategory.WARN);
 		addEEnumLiteral(logCategoryEEnum, LogCategory.ERROR);
 		addEEnumLiteral(logCategoryEEnum, LogCategory.FATAL);
 
 		initEEnum(logLevelEEnum, LogLevel.class, "LogLevel");
 		addEEnumLiteral(logLevelEEnum, LogLevel.SIMPLE);
 		addEEnumLiteral(logLevelEEnum, LogLevel.HEADERS);
 		addEEnumLiteral(logLevelEEnum, LogLevel.FULL);
 		addEEnumLiteral(logLevelEEnum, LogLevel.CUSTOM);
 
 		initEEnum(endPointAddressingVersionEEnum, EndPointAddressingVersion.class, "EndPointAddressingVersion");
 		addEEnumLiteral(endPointAddressingVersionEEnum, EndPointAddressingVersion.FINAL);
 		addEEnumLiteral(endPointAddressingVersionEEnum, EndPointAddressingVersion.SUBMISSION);
 
 		initEEnum(endPointTimeOutActionEEnum, EndPointTimeOutAction.class, "EndPointTimeOutAction");
 		addEEnumLiteral(endPointTimeOutActionEEnum, EndPointTimeOutAction.DISCARD);
 		addEEnumLiteral(endPointTimeOutActionEEnum, EndPointTimeOutAction.FAULT);
 
 		initEEnum(endPointMessageFormatEEnum, EndPointMessageFormat.class, "EndPointMessageFormat");
 		addEEnumLiteral(endPointMessageFormatEEnum, EndPointMessageFormat.LEAVE_AS_IS);
 		addEEnumLiteral(endPointMessageFormatEEnum, EndPointMessageFormat.SOAP_11);
 		addEEnumLiteral(endPointMessageFormatEEnum, EndPointMessageFormat.SOAP_12);
 		addEEnumLiteral(endPointMessageFormatEEnum, EndPointMessageFormat.POX);
 		addEEnumLiteral(endPointMessageFormatEEnum, EndPointMessageFormat.GET);
 
 		initEEnum(endPointAttachmentOptimizationEEnum, EndPointAttachmentOptimization.class, "EndPointAttachmentOptimization");
 		addEEnumLiteral(endPointAttachmentOptimizationEEnum, EndPointAttachmentOptimization.LEAVE_AS_IS);
 		addEEnumLiteral(endPointAttachmentOptimizationEEnum, EndPointAttachmentOptimization.MTOM);
 		addEEnumLiteral(endPointAttachmentOptimizationEEnum, EndPointAttachmentOptimization.SWA);
 
 		initEEnum(propertyDataTypeEEnum, PropertyDataType.class, "PropertyDataType");
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.STRING);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.INTEGER);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.BOOLEAN);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.DOUBLE);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.FLOAT);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.LONG);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.SHORT);
 		addEEnumLiteral(propertyDataTypeEEnum, PropertyDataType.OM);
 
 		initEEnum(propertyActionEEnum, PropertyAction.class, "PropertyAction");
 		addEEnumLiteral(propertyActionEEnum, PropertyAction.SET);
 		addEEnumLiteral(propertyActionEEnum, PropertyAction.REMOVE);
 
 		initEEnum(propertyScopeEEnum, PropertyScope.class, "PropertyScope");
 		addEEnumLiteral(propertyScopeEEnum, PropertyScope.SYNAPSE);
 		addEEnumLiteral(propertyScopeEEnum, PropertyScope.TRANSPORT);
 		addEEnumLiteral(propertyScopeEEnum, PropertyScope.AXIS2);
 		addEEnumLiteral(propertyScopeEEnum, PropertyScope.AXIS2_CLIENT);
 
 		initEEnum(propertyValueTypeEEnum, PropertyValueType.class, "PropertyValueType");
 		addEEnumLiteral(propertyValueTypeEEnum, PropertyValueType.LITERAL);
 		addEEnumLiteral(propertyValueTypeEEnum, PropertyValueType.EXPRESSION);
 
 		initEEnum(enrichSourceTypeEEnum, EnrichSourceType.class, "EnrichSourceType");
 		addEEnumLiteral(enrichSourceTypeEEnum, EnrichSourceType.CUSTOM);
 		addEEnumLiteral(enrichSourceTypeEEnum, EnrichSourceType.ENVELOPE);
 		addEEnumLiteral(enrichSourceTypeEEnum, EnrichSourceType.BODY);
 		addEEnumLiteral(enrichSourceTypeEEnum, EnrichSourceType.PROPERTY);
 		addEEnumLiteral(enrichSourceTypeEEnum, EnrichSourceType.INLINE);
 
 		initEEnum(enrichTargetActionEEnum, EnrichTargetAction.class, "EnrichTargetAction");
 		addEEnumLiteral(enrichTargetActionEEnum, EnrichTargetAction.REPLACE);
 		addEEnumLiteral(enrichTargetActionEEnum, EnrichTargetAction.CHILD);
 		addEEnumLiteral(enrichTargetActionEEnum, EnrichTargetAction.SIBLING);
 
 		initEEnum(enrichTargetTypeEEnum, EnrichTargetType.class, "EnrichTargetType");
 		addEEnumLiteral(enrichTargetTypeEEnum, EnrichTargetType.CUSTOM);
 		addEEnumLiteral(enrichTargetTypeEEnum, EnrichTargetType.ENVELOPE);
 		addEEnumLiteral(enrichTargetTypeEEnum, EnrichTargetType.BODY);
 		addEEnumLiteral(enrichTargetTypeEEnum, EnrichTargetType.PROPERTY);
 
 		initEEnum(eventTopicTypeEEnum, EventTopicType.class, "EventTopicType");
 		addEEnumLiteral(eventTopicTypeEEnum, EventTopicType.STATIC);
 		addEEnumLiteral(eventTopicTypeEEnum, EventTopicType.DYNAMIC);
 
 		initEEnum(scriptTypeEEnum, ScriptType.class, "ScriptType");
 		addEEnumLiteral(scriptTypeEEnum, ScriptType.INLINE);
 		addEEnumLiteral(scriptTypeEEnum, ScriptType.REGISTRY_REFERENCE);
 
 		initEEnum(scriptLanguageEEnum, ScriptLanguage.class, "ScriptLanguage");
 		addEEnumLiteral(scriptLanguageEEnum, ScriptLanguage.JAVASCRIPT);
 		addEEnumLiteral(scriptLanguageEEnum, ScriptLanguage.RUBY);
 		addEEnumLiteral(scriptLanguageEEnum, ScriptLanguage.GROOVY);
 
 		initEEnum(faultSoapVersionEEnum, FaultSoapVersion.class, "FaultSoapVersion");
 		addEEnumLiteral(faultSoapVersionEEnum, FaultSoapVersion.SOAP_11);
 		addEEnumLiteral(faultSoapVersionEEnum, FaultSoapVersion.SOAP_12);
 
 		initEEnum(faultCodeSoap11EEnum, FaultCodeSoap11.class, "FaultCodeSoap11");
 		addEEnumLiteral(faultCodeSoap11EEnum, FaultCodeSoap11.VERSION_MISSMATCH);
 		addEEnumLiteral(faultCodeSoap11EEnum, FaultCodeSoap11.MUST_UNDERSTAND);
 		addEEnumLiteral(faultCodeSoap11EEnum, FaultCodeSoap11.CLIENT);
 		addEEnumLiteral(faultCodeSoap11EEnum, FaultCodeSoap11.SERVER);
 
 		initEEnum(faultCodeSoap12EEnum, FaultCodeSoap12.class, "FaultCodeSoap12");
 		addEEnumLiteral(faultCodeSoap12EEnum, FaultCodeSoap12.VERSION_MISSMATCH);
 		addEEnumLiteral(faultCodeSoap12EEnum, FaultCodeSoap12.MUST_UNDERSTAND);
 		addEEnumLiteral(faultCodeSoap12EEnum, FaultCodeSoap12.DATA_ENCODING_UNKNOWN);
 		addEEnumLiteral(faultCodeSoap12EEnum, FaultCodeSoap12.SENDER);
 		addEEnumLiteral(faultCodeSoap12EEnum, FaultCodeSoap12.RECEIVER);
 
 		initEEnum(faultStringTypeEEnum, FaultStringType.class, "FaultStringType");
 		addEEnumLiteral(faultStringTypeEEnum, FaultStringType.VALUE);
 		addEEnumLiteral(faultStringTypeEEnum, FaultStringType.EXPRESSION);
 
 		initEEnum(faultReasonTypeEEnum, FaultReasonType.class, "FaultReasonType");
 		addEEnumLiteral(faultReasonTypeEEnum, FaultReasonType.VALUE);
 		addEEnumLiteral(faultReasonTypeEEnum, FaultReasonType.EXPRESSION);
 
 		initEEnum(faultDetailTypeEEnum, FaultDetailType.class, "FaultDetailType");
 		addEEnumLiteral(faultDetailTypeEEnum, FaultDetailType.VALUE);
 		addEEnumLiteral(faultDetailTypeEEnum, FaultDetailType.EXPRESSION);
 
 		initEEnum(aggregateSequenceTypeEEnum, AggregateSequenceType.class, "AggregateSequenceType");
 		addEEnumLiteral(aggregateSequenceTypeEEnum, AggregateSequenceType.ANONYMOUS);
 		addEEnumLiteral(aggregateSequenceTypeEEnum, AggregateSequenceType.REGISTRY_REFERENCE);
 
 		initEEnum(targetSequenceTypeEEnum, TargetSequenceType.class, "TargetSequenceType");
 		addEEnumLiteral(targetSequenceTypeEEnum, TargetSequenceType.NONE);
 		addEEnumLiteral(targetSequenceTypeEEnum, TargetSequenceType.ANONYMOUS);
 		addEEnumLiteral(targetSequenceTypeEEnum, TargetSequenceType.REGISTRY_REFERENCE);
 
 		initEEnum(targetEndpointTypeEEnum, TargetEndpointType.class, "TargetEndpointType");
 		addEEnumLiteral(targetEndpointTypeEEnum, TargetEndpointType.NONE);
 		addEEnumLiteral(targetEndpointTypeEEnum, TargetEndpointType.ANONYMOUS);
 		addEEnumLiteral(targetEndpointTypeEEnum, TargetEndpointType.REGISTRY_REFERENCE);
 
 		initEEnum(cacheSequenceTypeEEnum, CacheSequenceType.class, "CacheSequenceType");
 		addEEnumLiteral(cacheSequenceTypeEEnum, CacheSequenceType.ANONYMOUS);
 		addEEnumLiteral(cacheSequenceTypeEEnum, CacheSequenceType.REGISTRY_REFERENCE);
 
 		initEEnum(cacheImplementationTypeEEnum, CacheImplementationType.class, "CacheImplementationType");
 		addEEnumLiteral(cacheImplementationTypeEEnum, CacheImplementationType.IN_MEMORY);
 
 		initEEnum(cacheActionEEnum, CacheAction.class, "CacheAction");
 		addEEnumLiteral(cacheActionEEnum, CacheAction.FINDER);
 		addEEnumLiteral(cacheActionEEnum, CacheAction.COLLECTOR);
 
 		initEEnum(cacheScopeEEnum, CacheScope.class, "CacheScope");
 		addEEnumLiteral(cacheScopeEEnum, CacheScope.PER_MEDIATOR);
 		addEEnumLiteral(cacheScopeEEnum, CacheScope.PER_HOST);
 
 		initEEnum(xQueryVariableTypeEEnum, XQueryVariableType.class, "XQueryVariableType");
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.DOCUMENT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.DOCUMENT_ELEMENT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.ELEMENT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.INT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.INTEGER);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.BOOLEAN);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.BYTE);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.DOUBLE);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.SHORT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.LONG);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.FLOAT);
 		addEEnumLiteral(xQueryVariableTypeEEnum, XQueryVariableType.STRING);
 
 		initEEnum(xQueryVariableValueTypeEEnum, XQueryVariableValueType.class, "XQueryVariableValueType");
 		addEEnumLiteral(xQueryVariableValueTypeEEnum, XQueryVariableValueType.LITERAL);
 		addEEnumLiteral(xQueryVariableValueTypeEEnum, XQueryVariableValueType.EXPRESSION);
 
 		initEEnum(calloutPayloadTypeEEnum, CalloutPayloadType.class, "CalloutPayloadType");
 		addEEnumLiteral(calloutPayloadTypeEEnum, CalloutPayloadType.MESSAGE_ELEMENT);
 		addEEnumLiteral(calloutPayloadTypeEEnum, CalloutPayloadType.REGISTRY_ENTRY);
 
 		initEEnum(calloutResultTypeEEnum, CalloutResultType.class, "CalloutResultType");
 		addEEnumLiteral(calloutResultTypeEEnum, CalloutResultType.MESSAGE_ELEMENT);
 		addEEnumLiteral(calloutResultTypeEEnum, CalloutResultType.CONTEXT_PROPERTY);
 
 		initEEnum(rmSpecVersionEEnum, RMSpecVersion.class, "RMSpecVersion");
 		addEEnumLiteral(rmSpecVersionEEnum, RMSpecVersion.VERSION_10);
 		addEEnumLiteral(rmSpecVersionEEnum, RMSpecVersion.VERSION_11);
 
 		initEEnum(rmSequenceTypeEEnum, RMSequenceType.class, "RMSequenceType");
 		addEEnumLiteral(rmSequenceTypeEEnum, RMSequenceType.SINGLE_MESSAGE);
 		addEEnumLiteral(rmSequenceTypeEEnum, RMSequenceType.CORRELATED_SEQUENCE);
 
 		initEEnum(transactionActionEEnum, TransactionAction.class, "TransactionAction");
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.COMMIT);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.FAULT_IF_NO_TRANSACTION);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.INITIATE_NEW);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.RESUME);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.SUSPEND);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.ROLLBACK);
 		addEEnumLiteral(transactionActionEEnum, TransactionAction.USE_EXISTING_OR_NEW);
 
 		initEEnum(headerActionEEnum, HeaderAction.class, "HeaderAction");
 		addEEnumLiteral(headerActionEEnum, HeaderAction.SET);
 		addEEnumLiteral(headerActionEEnum, HeaderAction.REMOVE);
 
 		initEEnum(headerValueTypeEEnum, HeaderValueType.class, "HeaderValueType");
 		addEEnumLiteral(headerValueTypeEEnum, HeaderValueType.LITERAL);
 		addEEnumLiteral(headerValueTypeEEnum, HeaderValueType.EXPRESSION);
 
 		initEEnum(throttlePolicyTypeEEnum, ThrottlePolicyType.class, "ThrottlePolicyType");
 		addEEnumLiteral(throttlePolicyTypeEEnum, ThrottlePolicyType.INLINE);
 		addEEnumLiteral(throttlePolicyTypeEEnum, ThrottlePolicyType.REGISTRY_REFERENCE);
 
 		initEEnum(throttleConditionTypeEEnum, ThrottleConditionType.class, "ThrottleConditionType");
 		addEEnumLiteral(throttleConditionTypeEEnum, ThrottleConditionType.IP);
 		addEEnumLiteral(throttleConditionTypeEEnum, ThrottleConditionType.DOMAIN);
 
 		initEEnum(throttleAccessTypeEEnum, ThrottleAccessType.class, "ThrottleAccessType");
 		addEEnumLiteral(throttleAccessTypeEEnum, ThrottleAccessType.ALLOW);
 		addEEnumLiteral(throttleAccessTypeEEnum, ThrottleAccessType.DENY);
 		addEEnumLiteral(throttleAccessTypeEEnum, ThrottleAccessType.CONTROL);
 
 		initEEnum(throttleSequenceTypeEEnum, ThrottleSequenceType.class, "ThrottleSequenceType");
 		addEEnumLiteral(throttleSequenceTypeEEnum, ThrottleSequenceType.ANONYMOUS);
 		addEEnumLiteral(throttleSequenceTypeEEnum, ThrottleSequenceType.REGISTRY_REFERENCE);
 
 		initEEnum(commandPropertyValueTypeEEnum, CommandPropertyValueType.class, "CommandPropertyValueType");
 		addEEnumLiteral(commandPropertyValueTypeEEnum, CommandPropertyValueType.LITERAL);
 		addEEnumLiteral(commandPropertyValueTypeEEnum, CommandPropertyValueType.MESSAGE_ELEMENT);
 		addEEnumLiteral(commandPropertyValueTypeEEnum, CommandPropertyValueType.CONTEXT_PROPERTY);
 
 		initEEnum(commandPropertyMessageActionEEnum, CommandPropertyMessageAction.class, "CommandPropertyMessageAction");
 		addEEnumLiteral(commandPropertyMessageActionEEnum, CommandPropertyMessageAction.READ_MESSAGE);
 		addEEnumLiteral(commandPropertyMessageActionEEnum, CommandPropertyMessageAction.UPDATE_MESSAGE);
 		addEEnumLiteral(commandPropertyMessageActionEEnum, CommandPropertyMessageAction.READ_AND_UPDATE_MESSAGE);
 
 		initEEnum(commandPropertyContextActionEEnum, CommandPropertyContextAction.class, "CommandPropertyContextAction");
 		addEEnumLiteral(commandPropertyContextActionEEnum, CommandPropertyContextAction.READ_CONTEXT);
 		addEEnumLiteral(commandPropertyContextActionEEnum, CommandPropertyContextAction.UPDATE_CONTEXT);
 		addEEnumLiteral(commandPropertyContextActionEEnum, CommandPropertyContextAction.READ_AND_UPDATE_CONTEXT);
 
 		initEEnum(sqlExecutorConnectionTypeEEnum, SqlExecutorConnectionType.class, "SqlExecutorConnectionType");
 		addEEnumLiteral(sqlExecutorConnectionTypeEEnum, SqlExecutorConnectionType.DB_CONNECTION);
 		addEEnumLiteral(sqlExecutorConnectionTypeEEnum, SqlExecutorConnectionType.DATA_SOURCE);
 
 		initEEnum(sqlExecutorDatasourceTypeEEnum, SqlExecutorDatasourceType.class, "SqlExecutorDatasourceType");
 		addEEnumLiteral(sqlExecutorDatasourceTypeEEnum, SqlExecutorDatasourceType.EXTERNAL);
 		addEEnumLiteral(sqlExecutorDatasourceTypeEEnum, SqlExecutorDatasourceType.CARBON);
 
 		initEEnum(sqlExecutorBooleanValueEEnum, SqlExecutorBooleanValue.class, "SqlExecutorBooleanValue");
 		addEEnumLiteral(sqlExecutorBooleanValueEEnum, SqlExecutorBooleanValue.FALSE);
 		addEEnumLiteral(sqlExecutorBooleanValueEEnum, SqlExecutorBooleanValue.TRUE);
 		addEEnumLiteral(sqlExecutorBooleanValueEEnum, SqlExecutorBooleanValue.DEFAULT);
 
 		initEEnum(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.class, "SqlExecutorIsolationLevel");
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.TRANSACTION_NONE);
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.TRANSACTION_READ_COMMITTED);
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.TRANSACTION_READ_UNCOMMITTED);
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.TRANSACTION_REPEATABLE_READ);
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.TRANSACTION_SERIALIZABLE);
 		addEEnumLiteral(sqlExecutorIsolationLevelEEnum, SqlExecutorIsolationLevel.DEFAULT);
 
 		initEEnum(sqlParameterValueTypeEEnum, SqlParameterValueType.class, "SqlParameterValueType");
 		addEEnumLiteral(sqlParameterValueTypeEEnum, SqlParameterValueType.LITERAL);
 		addEEnumLiteral(sqlParameterValueTypeEEnum, SqlParameterValueType.EXPRESSION);
 
 		initEEnum(sqlParameterDataTypeEEnum, SqlParameterDataType.class, "SqlParameterDataType");
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.CHAR);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.VARCHAR);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.LONGVARCHAR);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.NUMERIC);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.DECIMAL);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.BIT);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.TINYINT);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.SMALLINT);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.INTEGER);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.BIGINT);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.REAL);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.FLOAT);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.DOUBLE);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.DATE);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.TIME);
 		addEEnumLiteral(sqlParameterDataTypeEEnum, SqlParameterDataType.TIMESTAMP);
 
 		initEEnum(ruleActionsEEnum, RuleActions.class, "RuleActions");
 		addEEnumLiteral(ruleActionsEEnum, RuleActions.REPLACE);
 		addEEnumLiteral(ruleActionsEEnum, RuleActions.CHILD);
 		addEEnumLiteral(ruleActionsEEnum, RuleActions.SIBLING);
 
 		initEEnum(ruleSourceTypeEEnum, RuleSourceType.class, "RuleSourceType");
 		addEEnumLiteral(ruleSourceTypeEEnum, RuleSourceType.INLINE);
 		addEEnumLiteral(ruleSourceTypeEEnum, RuleSourceType.REGISTRY_REFERENCE);
 
 		initEEnum(ruleFactTypeEEnum, RuleFactType.class, "RuleFactType");
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.CUSTOM);
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.DOM);
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.MESSAGE);
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.CONTEXT);
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.OMELEMENT);
 		addEEnumLiteral(ruleFactTypeEEnum, RuleFactType.MEDIATOR);
 
 		initEEnum(ruleFactValueTypeEEnum, RuleFactValueType.class, "RuleFactValueType");
 		addEEnumLiteral(ruleFactValueTypeEEnum, RuleFactValueType.NONE);
 		addEEnumLiteral(ruleFactValueTypeEEnum, RuleFactValueType.LITERAL);
 		addEEnumLiteral(ruleFactValueTypeEEnum, RuleFactValueType.EXPRESSION);
 		addEEnumLiteral(ruleFactValueTypeEEnum, RuleFactValueType.REGISTRY_REFERENCE);
 
 		initEEnum(ruleResultTypeEEnum, RuleResultType.class, "RuleResultType");
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.CUSTOM);
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.DOM);
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.MESSAGE);
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.CONTEXT);
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.OMELEMENT);
 		addEEnumLiteral(ruleResultTypeEEnum, RuleResultType.MEDIATOR);
 
 		initEEnum(ruleResultValueTypeEEnum, RuleResultValueType.class, "RuleResultValueType");
 		addEEnumLiteral(ruleResultValueTypeEEnum, RuleResultValueType.LITERAL);
 		addEEnumLiteral(ruleResultValueTypeEEnum, RuleResultValueType.EXPRESSION);
 		addEEnumLiteral(ruleResultValueTypeEEnum, RuleResultValueType.REGISTRY_REFERENCE);
 
 		initEEnum(ruleOptionTypeEEnum, RuleOptionType.class, "RuleOptionType");
 		addEEnumLiteral(ruleOptionTypeEEnum, RuleOptionType.VALUE);
 		addEEnumLiteral(ruleOptionTypeEEnum, RuleOptionType.EXPRESSION);
 
 		initEEnum(smooksIODataTypeEEnum, SmooksIODataType.class, "SmooksIODataType");
 		addEEnumLiteral(smooksIODataTypeEEnum, SmooksIODataType.XML);
 		addEEnumLiteral(smooksIODataTypeEEnum, SmooksIODataType.TEXT);
 
 		initEEnum(expressionActionEEnum, ExpressionAction.class, "ExpressionAction");
 		addEEnumLiteral(expressionActionEEnum, ExpressionAction.ADD);
 		addEEnumLiteral(expressionActionEEnum, ExpressionAction.REPLACE);
 		addEEnumLiteral(expressionActionEEnum, ExpressionAction.SIBLING);
 
 		initEEnum(outputMethodEEnum, OutputMethod.class, "OutputMethod");
 		addEEnumLiteral(outputMethodEEnum, OutputMethod.DEFAULT);
 		addEEnumLiteral(outputMethodEEnum, OutputMethod.PROPERTY);
 		addEEnumLiteral(outputMethodEEnum, OutputMethod.EXPRESSION);
 
 		initEEnum(receivingSequenceTypeEEnum, ReceivingSequenceType.class, "ReceivingSequenceType");
 		addEEnumLiteral(receivingSequenceTypeEEnum, ReceivingSequenceType.DEFAULT);
 		addEEnumLiteral(receivingSequenceTypeEEnum, ReceivingSequenceType.STATIC);
 		addEEnumLiteral(receivingSequenceTypeEEnum, ReceivingSequenceType.DYNAMIC);
 
 		initEEnum(keyTypeEEnum, KeyType.class, "KeyType");
 		addEEnumLiteral(keyTypeEEnum, KeyType.STATIC);
 		addEEnumLiteral(keyTypeEEnum, KeyType.DYNAMIC);
 
 		initEEnum(payloadFactoryArgumentTypeEEnum, PayloadFactoryArgumentType.class, "PayloadFactoryArgumentType");
 		addEEnumLiteral(payloadFactoryArgumentTypeEEnum, PayloadFactoryArgumentType.VALUE);
 		addEEnumLiteral(payloadFactoryArgumentTypeEEnum, PayloadFactoryArgumentType.EXPRESSION);
 
 		initEEnum(typeEEnum, Type.class, "Type");
 		addEEnumLiteral(typeEEnum, Type.HTTP);
 		addEEnumLiteral(typeEEnum, Type.SIMPLE_CLIENT_SESSION);
 
 		initEEnum(localEntryValueTypeEEnum, LocalEntryValueType.class, "LocalEntryValueType");
 		addEEnumLiteral(localEntryValueTypeEEnum, LocalEntryValueType.LITERAL);
 		addEEnumLiteral(localEntryValueTypeEEnum, LocalEntryValueType.XML);
 		addEEnumLiteral(localEntryValueTypeEEnum, LocalEntryValueType.URL);
 
 		initEEnum(ruleActionTypeEEnum, RuleActionType.class, "RuleActionType");
 		addEEnumLiteral(ruleActionTypeEEnum, RuleActionType.REPLACE);
 		addEEnumLiteral(ruleActionTypeEEnum, RuleActionType.REMOVE);
 		addEEnumLiteral(ruleActionTypeEEnum, RuleActionType.APPEND);
 		addEEnumLiteral(ruleActionTypeEEnum, RuleActionType.PREPEND);
 		addEEnumLiteral(ruleActionTypeEEnum, RuleActionType.SET);
 
 		initEEnum(ruleFragmentTypeEEnum, RuleFragmentType.class, "RuleFragmentType");
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.PROTOCOL);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.HOST);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.PORT);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.PATH);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.QUERY);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.REF);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.USER);
 		addEEnumLiteral(ruleFragmentTypeEEnum, RuleFragmentType.FULL);
 
 		initEEnum(templateTypeEEnum, TemplateType.class, "TemplateType");
 		addEEnumLiteral(templateTypeEEnum, TemplateType.SEQUENCE);
 		addEEnumLiteral(templateTypeEEnum, TemplateType.ENDPOINT);
 
 		initEEnum(taskPropertyTypeEEnum, TaskPropertyType.class, "TaskPropertyType");
 		addEEnumLiteral(taskPropertyTypeEEnum, TaskPropertyType.LITERAL);
 		addEEnumLiteral(taskPropertyTypeEEnum, TaskPropertyType.XML);
 
 		initEEnum(taskTriggerTypeEEnum, TaskTriggerType.class, "TaskTriggerType");
 		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.SIMPLE);
 		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.CRON);
 
 		initEEnum(apiResourceUrlStyleEEnum, ApiResourceUrlStyle.class, "ApiResourceUrlStyle");
 		addEEnumLiteral(apiResourceUrlStyleEEnum, ApiResourceUrlStyle.NONE);
 		addEEnumLiteral(apiResourceUrlStyleEEnum, ApiResourceUrlStyle.URI_TEMPLATE);
 		addEEnumLiteral(apiResourceUrlStyleEEnum, ApiResourceUrlStyle.URL_MAPPING);
 
 		// Initialize data types
 		initEDataType(mapEDataType, Map.class, "Map", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 
 		// Create resource
 		createResource(eNS_URI);
 	}
 
 } //EsbPackageImpl
