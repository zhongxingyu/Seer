 package org.mule.module.magento.automation;
 
 import org.junit.experimental.categories.Categories;
 import org.junit.experimental.categories.Categories.IncludeCategory;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite.SuiteClasses;
 import org.mule.module.magento.automation.testcases.AddCategoryProductTestCases;
 import org.mule.module.magento.automation.testcases.AddOrderCommentTestCases;
 import org.mule.module.magento.automation.testcases.AddOrderInvoiceCommentTestCases;
 import org.mule.module.magento.automation.testcases.AddOrderShipmentCommentTestCases;
 import org.mule.module.magento.automation.testcases.AddOrderShipmentTrackTestCases;
 import org.mule.module.magento.automation.testcases.AddProductLinkTestCases;
 import org.mule.module.magento.automation.testcases.AddShoppingCartCouponTestCases;
 import org.mule.module.magento.automation.testcases.AddShoppingCartProductTestCases;
 import org.mule.module.magento.automation.testcases.CancelOrderInvoiceTestCases;
 import org.mule.module.magento.automation.testcases.CancelOrderTestCases;
 import org.mule.module.magento.automation.testcases.CaptureOrderInvoiceTestCases;
 import org.mule.module.magento.automation.testcases.CreateCategoryTestCases;
 import org.mule.module.magento.automation.testcases.CreateCustomerAddressTestCases;
 import org.mule.module.magento.automation.testcases.CreateCustomerTestCases;
 import org.mule.module.magento.automation.testcases.CreateOrderInvoiceTestCases;
 import org.mule.module.magento.automation.testcases.CreateOrderShipmentTestCases;
 import org.mule.module.magento.automation.testcases.CreateProductAttributeMediaTestCases;
 import org.mule.module.magento.automation.testcases.CreateProductTestCases;
 import org.mule.module.magento.automation.testcases.CreateShoppingCartOrderTestCases;
 import org.mule.module.magento.automation.testcases.CreateShoppingCartTestCases;
 import org.mule.module.magento.automation.testcases.DeleteCategoryProductTestCases;
 import org.mule.module.magento.automation.testcases.DeleteCategoryTestCases;
 import org.mule.module.magento.automation.testcases.DeleteCustomerAddressTestCases;
 import org.mule.module.magento.automation.testcases.DeleteCustomerTestCases;
 import org.mule.module.magento.automation.testcases.DeleteOrderShipmentTrackTestCases;
 import org.mule.module.magento.automation.testcases.DeleteProductAttributeMediaTestCases;
 import org.mule.module.magento.automation.testcases.DeleteProductLinkTestCases;
 import org.mule.module.magento.automation.testcases.DeleteProductTestCases;
 import org.mule.module.magento.automation.testcases.GetCatalogCurrentStoreViewTestCases;
 import org.mule.module.magento.automation.testcases.GetCategoryTestCases;
 import org.mule.module.magento.automation.testcases.GetCategoryTreeTestCases;
 import org.mule.module.magento.automation.testcases.GetCustomerAddressTestCases;
 import org.mule.module.magento.automation.testcases.GetCustomerTestCases;
 import org.mule.module.magento.automation.testcases.GetInfoShoppingCartTestCases;
 import org.mule.module.magento.automation.testcases.GetOrderInvoiceTestCases;
 import org.mule.module.magento.automation.testcases.GetOrderShipmentCarriersTestCases;
 import org.mule.module.magento.automation.testcases.GetOrderShipmentTestCases;
 import org.mule.module.magento.automation.testcases.GetOrderTestCases;
 import org.mule.module.magento.automation.testcases.HoldOrderTestCases;
 import org.mule.module.magento.automation.testcases.ListCategoryAttributeOptionsTestCases;
 import org.mule.module.magento.automation.testcases.ListCategoryAttributesTestCases;
 import org.mule.module.magento.automation.testcases.ListCategoryProductsTestCases;
 import org.mule.module.magento.automation.testcases.ListCustomerAddressesTestCases;
 import org.mule.module.magento.automation.testcases.ListCustomerGroupsTestCases;
 import org.mule.module.magento.automation.testcases.ListCustomersTestCases;
 import org.mule.module.magento.automation.testcases.ListDirectoryCountriesTestCases;
 import org.mule.module.magento.automation.testcases.ListDirectoryRegionsTestCases;
 import org.mule.module.magento.automation.testcases.ListInventoryStockItemsTestCases;
 import org.mule.module.magento.automation.testcases.ListOrdersInvoicesTestCases;
 import org.mule.module.magento.automation.testcases.ListOrdersShipmentsTestCases;
 import org.mule.module.magento.automation.testcases.ListOrdersTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributeMediaTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributeMediaTypesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributeOptionsTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributeSetsTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributeTierPricesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductAttributesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductLinkAttributesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductLinkTestCases;
 import org.mule.module.magento.automation.testcases.ListProductLinkTypesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductTypesTestCases;
 import org.mule.module.magento.automation.testcases.ListProductsTestCases;
 import org.mule.module.magento.automation.testcases.ListShoppingCartLicensesTestCases;
 import org.mule.module.magento.automation.testcases.ListShoppingCartPaymentMethodsTestCases;
 import org.mule.module.magento.automation.testcases.ListShoppingCartShippingMethodsTestCases;
 import org.mule.module.magento.automation.testcases.ListShoppingCartTotalsTestCases;
 import org.mule.module.magento.automation.testcases.ListStockItemsTestCases;
 import org.mule.module.magento.automation.testcases.MoveCategoryTestCases;
 import org.mule.module.magento.automation.testcases.RegressionTests;
 import org.mule.module.magento.automation.testcases.RemoveShoppingCartCouponTestCases;
 import org.mule.module.magento.automation.testcases.RemoveShoppingCartProductTestCases;
 import org.mule.module.magento.automation.testcases.SetShoppingCartCustomerAddressesTestCases;
 import org.mule.module.magento.automation.testcases.SetShoppingCartCustomerTestCases;
 import org.mule.module.magento.automation.testcases.SetShoppingCartPaymentMethodTestCases;
 import org.mule.module.magento.automation.testcases.SetShoppingCartShippingMethodTestCases;
 import org.mule.module.magento.automation.testcases.UnholdOrderTestCases;
 import org.mule.module.magento.automation.testcases.UpdateCategoryAttributeStoreViewTestCases;
 import org.mule.module.magento.automation.testcases.UpdateCategoryProductTestCases;
 import org.mule.module.magento.automation.testcases.UpdateCategoryTestCases;
 import org.mule.module.magento.automation.testcases.UpdateCustomerAddressTestCases;
 import org.mule.module.magento.automation.testcases.UpdateCustomerTestCases;
 import org.mule.module.magento.automation.testcases.UpdateInventoryStockItemTestCases;
 import org.mule.module.magento.automation.testcases.UpdateProductAttributeMediaTestCases;
 import org.mule.module.magento.automation.testcases.UpdateProductAttributeTierPriceTestCases;
 import org.mule.module.magento.automation.testcases.UpdateProductLinkTestCases;
 import org.mule.module.magento.automation.testcases.UpdateProductSpecialPriceTestCases;
 import org.mule.module.magento.automation.testcases.UpdateProductTestCases;
 import org.mule.module.magento.automation.testcases.UpdateStockItemTestCases;
 import org.mule.module.magento.automation.testcases.VoidOrderInvoiceTestCases;
 
 @RunWith(Categories.class)
 @IncludeCategory(RegressionTests.class)
 @SuiteClasses({
 	AddCategoryProductTestCases.class,
 	AddOrderCommentTestCases.class,
 	AddOrderInvoiceCommentTestCases.class,
 	AddOrderShipmentCommentTestCases.class,
 	AddOrderShipmentTrackTestCases.class,
 	AddProductLinkTestCases.class,
 	AddShoppingCartCouponTestCases.class,
 	AddShoppingCartProductTestCases.class,
 	CancelOrderInvoiceTestCases.class,
 	CancelOrderTestCases.class,
 	CaptureOrderInvoiceTestCases.class,
 	CreateCategoryTestCases.class,
 	CreateCustomerAddressTestCases.class,
 	CreateCustomerTestCases.class,
 	CreateOrderInvoiceTestCases.class,
 	CreateOrderShipmentTestCases.class,
 	CreateProductAttributeMediaTestCases.class,
 	CreateProductTestCases.class,
 	CreateShoppingCartOrderTestCases.class,
 	CreateShoppingCartTestCases.class,
 	DeleteCategoryProductTestCases.class,
 	DeleteCategoryTestCases.class,
 	DeleteCustomerAddressTestCases.class,
 	DeleteCustomerTestCases.class,
 	DeleteOrderShipmentTrackTestCases.class,
 	DeleteProductAttributeMediaTestCases.class,
 	DeleteProductLinkTestCases.class,
 	DeleteProductTestCases.class,
 	GetCatalogCurrentStoreViewTestCases.class,
 	GetCategoryTestCases.class,
 	GetCategoryTreeTestCases.class,
 	GetCustomerAddressTestCases.class,
 	GetCustomerTestCases.class,
 	GetInfoShoppingCartTestCases.class,
 	GetOrderInvoiceTestCases.class,
 	GetOrderShipmentCarriersTestCases.class,
 	GetOrderShipmentTestCases.class,
 	GetOrderTestCases.class,
 	HoldOrderTestCases.class,
 	ListCategoryAttributeOptionsTestCases.class,
 	ListCategoryAttributesTestCases.class,
 	ListCategoryLevelsTestCases.class,
 	ListCategoryProductsTestCases.class,
 	ListCustomerAddressesTestCases.class,
 	ListCustomerGroupsTestCases.class,
 	ListCustomersTestCases.class,
 	ListDirectoryCountriesTestCases.class,
 	ListDirectoryRegionsTestCases.class,
 	ListInventoryStockItemsTestCases.class,
 	ListOrdersInvoicesTestCases.class,
 	ListOrdersShipmentsTestCases.class,
 	ListOrdersTestCases.class,
 	ListProductAttributeMediaTestCases.class,
 	ListProductAttributeMediaTypesTestCases.class,
 	ListProductAttributeOptionsTestCases.class,
 	ListProductAttributeSetsTestCases.class,
 	ListProductAttributesTestCases.class,
 	ListProductAttributeTierPricesTestCases.class,
 	ListProductLinkAttributesTestCases.class,
 	ListProductLinkTestCases.class,
 	ListProductLinkTypesTestCases.class,
 	ListProductsTestCases.class,
 	ListProductTypesTestCases.class,
 	ListShoppingCartLicensesTestCases.class,
 	ListShoppingCartPaymentMethodsTestCases.class,
 	ListShoppingCartShippingMethodsTestCases.class,
 	ListShoppingCartTotalsTestCases.class,
 	ListStockItemsTestCases.class,
 	MoveCategoryTestCases.class,
 	RemoveShoppingCartCouponTestCases.class,
 	RemoveShoppingCartProductTestCases.class,
 	SetShoppingCartCustomerAddressesTestCases.class,
 	SetShoppingCartCustomerTestCases.class,
 	SetShoppingCartPaymentMethodTestCases.class,
 	SetShoppingCartShippingMethodTestCases.class,
 	UnholdOrderTestCases.class,
 	UpdateCategoryAttributeStoreViewTestCases.class,
 	UpdateCategoryProductTestCases.class,
 	UpdateCategoryTestCases.class,
 	UpdateCustomerAddressTestCases.class,
 	UpdateCustomerTestCases.class,
 	UpdateInventoryStockItemTestCases.class,
 	UpdateProductAttributeMediaTestCases.class,
 	UpdateProductAttributeTierPriceTestCases.class,
 	UpdateProductLinkTestCases.class,
 	UpdateProductSpecialPriceTestCases.class,
 	UpdateProductTestCases.class,
 	UpdateStockItemTestCases.class,
 	VoidOrderInvoiceTestCases.class
 })
 public class RegressionTestSuite {
 
 }
